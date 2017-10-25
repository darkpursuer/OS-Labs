/****************************************************************************
*                                                                           *
* Author: Yi Zhang                                                          *
* Organization: NYU                                                         *
* Email: yz3940@nyu.edu                                                     *
* Date: 2017.1.29                                                           *
*                                                                           *
* This code is only for the Lab1 of the 'Operating System, Allan Gottlieb'  *
*                                                                           *
*****************************************************************************/

#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <cstring>
#include <vector>
#include <sys/io.h>
#include <unistd.h>
#include <sys/stat.h>
#include <dirent.h>
using namespace std;

static int MACHINESIZE = 200;

class Linker
{
	/*************************************
	*   Some required internal classes   *
	**************************************/

private:

	enum AddressType
	{
		I = 'I',
		A = 'A',
		R = 'R',
		E = 'E'
	};

	class Variable
	{
	public:
		Variable(string n, int l, int i)
		{
			name = n;
			location = l;
			moduleIndex = i;
			isUsed = false;
			isMultiDefined = false;
			isExceeds = false;
		};
	public:
		string name;
		int location;
		int moduleIndex;
		bool isUsed;
		bool isMultiDefined;
		bool isExceeds;
	};

	class Usage
	{
	public:
		Usage(string n)
		{
			name = n;
		};
	public:
		string name;
		vector<int> locations;
	};

	class ProgramText
	{
	public:
		ProgramText(AddressType t, string addr)
		{
			type = t;
			address = addr;
			isChanged = false;
		};
	public:
		AddressType type;
		string address;
		bool isChanged;
	};

	enum ExceptionType
	{
		Warning,
		Error,

		MultiDefinedError,
		NotDefinedError,
		MultipleSymbolsInOneLineError,
		DefinitionExceedsSizeError,
		UseExceedsSizeError,
		AbsoluteAddressExceedsSizeError,
		RelativeAddressExceedsSizeError,

		NotUsedWarning
	};

	class Exception
	{
	public:
		Exception(ExceptionType t, string m)
		{
			type = t;
			argument1 = m;
		};
	public:
		ExceptionType type;
		string argument1;
		string argument2;
		string toString() {
			string result = "";
			switch (type)
			{
			case Error:
				result = "Error: " + argument1;
				break;

			case Warning:
				result = "Warning: " + argument1;
				break;

			case MultiDefinedError:
				result = "Error: This variable " + argument1 + " is multiply defined; first value used.";
				break;

			case NotDefinedError:
				result = "Error: " + argument1 + " is not defined; zero used.";
				break;

			case MultipleSymbolsInOneLineError:
				result = "Error: Multiple variables used in instruction; all but first ignored.";
				break;

			case DefinitionExceedsSizeError:
				result = "Error: Definition exceeds module size; first word in module used.";
				break;

			case UseExceedsSizeError:
				result = "Error: Use of " + argument1 + " in module " + argument2 + " exceeds module size; use ignored.";
				break;

			case AbsoluteAddressExceedsSizeError:
				result = "Error: Absolute address exceeds machine size; zero used.";
				break;

			case RelativeAddressExceedsSizeError:
				result = "Error: Relative address exceeds module size; zero used.";
				break;

			default:
				result = argument1;
				break;
			}
			return result;
		};
	};

	/******************************
	*    Some global variables    *
	*******************************/

	string PATH;

	int COUNT;
	int PROGRAMSIZE;
	vector<Variable> variables;
	vector<vector<Usage> > usages;
	vector<vector<ProgramText> > texts;
	vector<Exception> exceptions;

	/**************************************************
	*    The constructor and main proceed function    *
	***************************************************/

public:

	Linker(string path, int machineSize = 200)
	{
		PATH = path;
		MACHINESIZE = machineSize;
		clearAllVariables();
	};

	string proceed()
	{
		string text = readFile(PATH);
		string report = scan(text);
		report = report + scanMemory();
		report = report + "\r\n" + validate();
		return report;
	}

private:

	// Clear the temporary global variables
	void clearAllVariables()
	{
		COUNT = 0;
		PROGRAMSIZE = 0;
		variables = {};
		usages = {};
		texts = {};
		exceptions = {};
	}

	// Scan the definitions, usages and the program instructions
	string scan(string text)
	{
		string result = "Symbol Table";
		int index = 0;
		int baseIndex = 0;

		// first get the number of modules
		string word = getNext(text, &index);
		COUNT = atoi(word.c_str());
		for (int mi = 0; mi < COUNT; mi++)
		{
			// get the number of delarations
			vector<Variable> thisModuleVars = {};
			word = getNext(text, &index);
			int declarationCount = atoi(word.c_str());
			if (declarationCount > 0)
			{
				for (int di = 0; di < declarationCount; di++)
				{
					// get the variable name
					word = getNext(text, &index);
					string variableName = word;

					// get the variable address
					word = getNext(text, &index);
					int location = atoi(word.c_str());

					// insert this variable into list
					Variable *var = getDefinition(&variables, variableName);
					if (var == 0)
					{
						Variable v(variableName, location, mi);
						variables.push_back(v);
						thisModuleVars.push_back(v);
					}
					else
					{
						// if exists, then throw an error
						var->isMultiDefined = true;
						throwException(MultiDefinedError, variableName);
					}
				}
			}

			// get the number of usages
			vector<Usage> usage = {};
			word = getNext(text, &index);
			int useCount = atoi(word.c_str());
			if (useCount > 0)
			{
				for (int ui = 0; ui < useCount; ui++)
				{
					word = getNext(text, &index);
					string variableName = word;
					vector<int> locations = {};

					word = getNext(text, &index);
					int location = atoi(word.c_str());
					while (location != -1)
					{
						locations.push_back(location);

						word = getNext(text, &index);
						location = atoi(word.c_str());
					}
					Usage u(variableName);
					u.locations = locations;
					usage.push_back(u);
				}
			}
			usages.push_back(usage);

			// get the number of program instructions
			vector<ProgramText> programText = {};
			word = getNext(text, &index);
			int programCount = atoi(word.c_str());
			if (programCount > 0)
			{
				for (int pi = 0; pi < programCount; pi++)
				{
					word = getNext(text, &index);
					AddressType type = AddressType(word[0]);

					word = getNext(text, &index);

					ProgramText p(type, word);
					programText.push_back(p);
				}
			}
			texts.push_back(programText);
			int programSize = programText.size();

			// check whether any variable exceeds this module
			for (int i = 0; i < thisModuleVars.size(); i++)
			{
				Variable *thisvar = &thisModuleVars[i];
				Variable *var = getDefinition(&variables, thisvar->name);
				if (var->location >= programSize)
				{
					var->isExceeds = true;
					var->location = 0;
					throwException(DefinitionExceedsSizeError, var->name);
				}
				var->location = baseIndex + var->location;
			}

			// check whether any usage exceeds this module
			for (int i = 0; i < usage.size(); i++)
			{
				Usage *u = &usage[i];
				for (int loci = 0; loci < u->locations.size(); loci++)
				{
					int loc = u->locations[loci];
					if (loc >= programSize)
					{
						Exception ex(UseExceedsSizeError, u->name);
						ex.argument2 = to_string(static_cast<long long unsigned int>(mi + 1));
						throwException(ex);
					}
				}
			}

			PROGRAMSIZE += programSize;
			baseIndex += programCount;
		}

		// check whether any variable is multiply defined or exceeds the module size
		for (int i = 0; i < variables.size(); i++)
		{
			Variable *var = &variables[i];
			result = result + "\r\n" + var->name + "=" + to_string(static_cast<long long unsigned int>(var->location));
			if (var->isMultiDefined)
			{
				result = result + " Error: This variable is multiply defined; first value used.";
			}
			if (var->isExceeds)
			{
				result = result + " Error: Definition exceeds module size; first word in module used.";
			}
		}
		result += "\r\n\r\n";
		return result;
	}

	// Scan the program instructions and handle with the memory map
	string scanMemory()
	{
		string result = "Memory Map";
		int baseIndex = 0;
		// first get the number of modules
		for (int mi = 0; mi < COUNT; mi++)
		{
			// scan the memory
			vector<ProgramText> *text = &texts[mi];
			vector<Usage> *usage = &usages[mi];
			int count = text->size();

			// modify all the relative addresses
			for (int line = 0; line < text->size(); line++)
			{
				ProgramText *t = &text->at(line);
				char index = t->address[0];
				int addr = atoi((t->address).substr(1, 3).c_str());
				string errorMessage = "";

				if (t->type == R)
				{
					int num = addr + baseIndex;
					if (num >= PROGRAMSIZE)
					{
						num = 0;
						Exception ex(RelativeAddressExceedsSizeError, "");
						errorMessage = ex.toString();
						throwException(ex);
					}
					string numText = to_string(static_cast<long long unsigned int>(num));
					while (numText.length() < 3)
					{
						numText = "0" + numText;
					}
					t->address = index + numText;
					t->isChanged = true;
				}
				else if (t->type == E)
				{
					// find whether there is a usage related to this instruction
					Usage *u = 0;
					vector<string> varnames = {};
					for (int ui = 0; ui < usage->size(); ui++)
					{
						Usage *utmp = &usage->at(ui);
						for (int loci = 0; loci < utmp->locations.size(); loci++)
						{
							int loc = utmp->locations[loci];
							if (loc == line)
							{
								if (u == 0)
								{
									u = utmp;
								}
								else
								{
									varnames.push_back(utmp->name);
									Exception ex(MultipleSymbolsInOneLineError, "");
									errorMessage = ex.toString();
									throwException(ex);
								}
								break;
							}
						}
					}

					// assign the address with this external variable
					int location = 0;
					Variable* var = getDefinition(&variables, u->name);
					if (var == 0)
					{
						Exception ex(NotDefinedError, u->name);
						errorMessage = ex.toString();
						throwException(ex);
					}
					else
					{
						location = var->location;
						var->isUsed = true;
					}
					string locationText = to_string(static_cast<long long unsigned int>(location));
					while (locationText.length() < 3)
					{
						locationText = "0" + locationText;
					}
					t->address = index + locationText;
					t->isChanged = true;

					// other multiply used variables should also be used
					for (int namei = 0; namei < varnames.size(); namei++)
					{
						string name = varnames[namei];
						Variable* mvar = getDefinition(&variables, name);
						if (mvar == 0)
						{
							Exception ex(NotDefinedError, name);
							errorMessage = ex.toString();
							throwException(ex);
						}
						else
						{
							mvar->isUsed = true;
						}
					}
				}
				else if (t->type == A)
				{
					if (addr >= MACHINESIZE)
					{
						string zeros = "000";
						t->address = index + zeros;

						Exception ex(AbsoluteAddressExceedsSizeError, "");
						errorMessage = ex.toString();
						throwException(ex);
					}
				}
				result = result + "\r\n" + to_string(static_cast<long long unsigned int>(baseIndex + line)) + ":\t" + t->address;
				if (errorMessage != "")
				{
					result = result + " " + errorMessage;
				}
			}
			baseIndex += count;
		}
		return result;
	}

	// Validate the input and print all the errors and warnings
	string validate()
	{
		string result = "";

		for (int i = 0; i < variables.size(); i++)
		{
			Variable v = variables[i];
			if (!v.isUsed)
			{
				throwException(Warning, v.name + " was defined in module " + to_string(static_cast<long long unsigned int>(v.moduleIndex + 1)) + " but never used.");
			}
		}

		for (int exi = 0; exi < exceptions.size(); exi++)
		{
			Exception ex = exceptions[exi];
			switch (ex.type)
			{
			case Warning:
			case Error:
			case NotUsedWarning:
			case UseExceedsSizeError:
				result = result + ex.toString() + "\r\n";
				break;

			default:
				break;
			}
		}

		return result;
	}

	// Read the input files
	string readFile(string path)
	{
		string text;
		string result = "";
		ifstream fin(path);
		while (getline(fin, text))
		{
			result = result + text + " ";
		}
		fin.close();
		return result;
	}

	// Get the next word and move forward the index
	string getNext(string text, int* index)
	{
		string result = "";
		if (*index < text.length())
		{
			char c = text[*index];
			while (c == ' ' || c == '\t' || c == '\r' || c == '\n')
			{
				(*index)++;
				if (*index >= text.length())
				{
					return "";
				}
				c = text[*index];
			}
			while (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
			{
				result = result + c;
				(*index)++;
				c = text[*index];
			}
		}
		return result;
	}

	// Find the variable definition with a specified name
	Variable *getDefinition(vector<Variable> *array, string name)
	{
		for (int i = 0; i < array->size(); i++)
		{
			Variable *var = &array->at(i);
			if (var->name == name)
			{
				return var;
			}
		}
		return 0;
	}

	// Throw exception
	void throwException(ExceptionType type, string message)
	{
		Exception ex(type, message);
		exceptions.push_back(ex);
	}

	// Throw exception
	void throwException(Exception ex)
	{
		exceptions.push_back(ex);
	}
};

void handle(string dirPath, string outputDirPath);

int main(int argc, char * argv[])
{
	// if there is no other argument
	if (argc == 1)
	{
		string dirPath, outputDirPath;
		cout << "Please insert or paste the directory path which includes all the input files:" << endl;
		getline(cin, dirPath);
		cout << "Then please insert or paste the directory path where the output files will be put in:" << endl;
		getline(cin, outputDirPath);

		handle(dirPath, outputDirPath);

		cout << "All tasks have been completed. Press any key to exit..." << endl;
		cin.get();
	}
	else if (argc == 4 && strcmp(argv[1], "-handle") == 0)
	{
		string dirPath = argv[2];
		string outputDirPath = argv[3];

		handle(dirPath, outputDirPath);

		cout << "All tasks have been completed. Press any key to exit..." << endl;
		cin.get();
	}
	else
	{
		cout << "Here are the help for this program." << endl;
		cout << "======================================================================================================" << endl;
		cout << " -help : Get help for this program." << endl;
		cout << "======================================================================================================" << endl;
		cout << " -handle [idpath] [odpath]: Start to handle with the input files, and then save the output files." << endl;
		cout << "       - [idpath] : The directory path which includes all the input files." << endl;
		cout << "       - [odpath] : The directory path where the output files will be put in." << endl;
		cout << "  Example: -handle ./inputs ./outputs" << endl;
		cout << "======================================================================================================" << endl;
		cout << endl;
	}


	return 0;
}

void handle(string dirPath, string outputDirPath)
{
	// get all the file names in the input directory
	vector<string> inputNames;
	DIR *hfile;
	struct dirent* fileinfo;
	if ((hfile = opendir(dirPath.c_str())))
	{
		while((fileinfo = readdir(hfile)) != 0)
		{
			if (strcmp(fileinfo->d_name, ".") != 0 && strcmp(fileinfo->d_name, "..") != 0)
			{
				inputNames.push_back(fileinfo->d_name);
			}
		}
		closedir(hfile);
	}

	// create the output directory if not exists
	if (!(hfile = opendir(outputDirPath.c_str())))
	{
		mkdir(outputDirPath.c_str(), S_IRUSR | S_IWUSR | S_IXUSR);
	}
	else
	{
		closedir(hfile);
	}

	// start proceed those files
	for (int namei = 0; namei < inputNames.size(); namei++)
	{
		string name = inputNames[namei];
		Linker linker(dirPath + "/" + name);
		string report = linker.proceed();

		// write the output in the files
		ofstream fout(outputDirPath + "/output-" + name);
		fout << report;
		fout.close();
	}
}

