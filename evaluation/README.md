## Evaluation Scripts

There are several scripts in this directory:
- `run.py`: the main script to run the evaluation
- `auto_annotate.py`: the script to automatically add the ctest-runner annotations
- `config.py`: the file that contains the configuration for the evaluation
- `utils.py`: the file that contains the utility functions for the evaluation


### How to run the evaluation
The supported projects can be found in `config.py` under the `SUPPORTE_PROJECTS` variable. 

To run the evaluation, simply run the following command:
```bash
$ python3 run.py <project_name>
```

The `run.py` script will have one output file named as `<project_name>-time.tsv` in the script running directory. 
The output file contains four time measurements for each project:
- `Vanilla_Test_Time`: the time to run the original test suite
- `Annotation_Time`: the time to add the ctest-runner annotations
- `Collection_Time`: the time to collect the ctest mapping files
- `Ctest_Test_Time`: the time to run the test with ctest-runner
