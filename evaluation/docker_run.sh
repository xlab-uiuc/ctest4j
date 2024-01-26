proj_list=("camel" "flink" "hbase" "zookeeper" "zeppelin" "kylin" "alluxio" "hcommon" "yarn" "mapreduce" "hdfs")
imageTag=$1
if [ -z "$imageTag" ]; then
	echo "Please specify the image tag"
	exit 1
fi

# Initialize a counter for completed projects
completed=0
total_projects=${#proj_list[@]}
# Function to update and print the progress bar
print_progress() {
    local project="$1"
    completed=$((completed + 1))
    percentage=$((completed * 100 / total_projects))
    #    echo -ne "Progress: ["
    echo -ne "Project: $project ["
    for ((i=0; i<percentage; i++)); do
        echo -n "="
    done
    for ((i=percentage; i<100; i++)); do
        echo -n " "
    done
    echo -ne "] $percentage% ($completed/$total_projects)\r"
}


for proj in ${proj_list[@]};
do
    print_progress "$proj"
    containerName=ctest-${proj}
    docker run --name ${containerName} -w "/home/ctestrunner/ctest4j/evaluation" -d -i -t "shuaiwang516/runner-image:${imageTag}" bash > /dev/null
    docker exec ${containerName} python3 run.py ${proj} > ${proj}.log 2>&1
    docker cp ${containerName}:/home/ctestrunner/ctest4j/evaluation/${proj}-time.tsv ./
done
	  
