import re
from argparse import ArgumentParser
from pathlib import Path

NUM_PATTERN = r"Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)(?!.*Time elapsed)"
TIME_PATTERN = r"Total time:\s+(\d{2}:\d{2}\s+h|\d+\.\d+\s+s|\d{2}:\d{2}\s+min)"


def read_and_extract(input_file):
    """
    Reads the input file and extracts the required data.
    Returns a list of tuples containing the extracted data.
    """
    content = Path(input_file).read_text()
    num_matches = re.findall(NUM_PATTERN, content)
    time_matches = re.findall(TIME_PATTERN, content)
    print(time_matches)
    if len(num_matches) != len(time_matches):
        raise ValueError("Mismatch in the number of number and time entries found.")
    return [
        (num_match + (time_match,))
        for num_match, time_match in zip(num_matches, time_matches)
    ]


def write_output(data, output_file):
    """
    Writes the extracted data to the output file.
    """
    with open(output_file, "w") as f:
        for line in data:
            f.write("\t".join(line) + "\n")


def parse_arguments():
    """
    Parses command-line arguments.
    """
    parser = ArgumentParser(description="Parse a log file and extract test results.")
    parser.add_argument("input_file", help="Log file to parse")
    parser.add_argument("output_file", help="File to write the output to")
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_arguments()
    data = read_and_extract(args.input_file)
    write_output(data, args.output_file)
