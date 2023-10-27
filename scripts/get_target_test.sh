DIR=$1

if [ -z "$DIR" ]; then
	echo "Usage: get_target_test.sh <path>"
	exit 1
fi

find ${DIR} -type f -name "*Test*" -exec grep -q '@Test' {} \; -exec grep -L 'Parameteri*' {} \; > all_test.txt
cat all_test.txt | cut -d'/' -f5- | sed -e 's/\//./g' -e 's/java/class,/g' > target_test.txt
echo $(cat target_test.txt | wc -l) "tests are possible to be transformed to configuration tests, see target_test.txt."
rm all_test.txt
