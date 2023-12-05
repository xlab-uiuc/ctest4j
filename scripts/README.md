# CTest Auto Annotation Script

## Functional overview (Instrumentation required)
`auto_annotate_script` is for design A, `auto_annotate_script_2` is for design B.
1. Add dependency to `pom.xml`. If repeated, skip.
2. Add import information, `@RunWith(CTestJUnit4Runner.class)`/`@RunWith(CTestJUnit4Runner2.class)` and `@CTestClass` to all target test classes.
3. Compile and run all the tests for parameter tracking.
4. Add `configMappingFile="${file_path}"` to the corresponding test class.
5. Compile and run all the tests again for checking.
6. Log files from step 3 and 5 will be saved to `"log"` directory.

## Steps to run:
1. Put the target project in the `project_dir` relative to `"ctest-runner/script"`.
2. Run `python auto_annotate.py ${project} ${test_module} ${project_dir} ${project_test_dir} $ctest_mapping_dir` (`${project_test_dir}` usually can be `"."`).

#### PS: Example commands can be found above the main function in `auto_annotate.py`.