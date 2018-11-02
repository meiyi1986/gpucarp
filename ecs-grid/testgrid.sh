#$ -S /bin/sh
#$ -wd /vol/grid-solar/sgeusers/yimei
##$ -M Fred.Bloggs@ecs.vuw.ac.nz 
##$ -m be 

ALGO="reactivegp"
DATASET=$1
INSTANCEID=$2
INSTANCE=$1$2
GRID_PATH="/vol/grid-solar/sgeusers/yimei/gphhucarp"
JAR_PATH=$GRID_PATH"/package"
DATA_PATH=$GRID_PATH"/data"
ALGO_PATH=$GRID_PATH/$ALGO

mkdir -p /local/tmp/yimei/$JOB_ID 

if [ -d /local/tmp/yimei/$JOB_ID ]; then
        cd /local/tmp/yimei/$JOB_ID
else
        echo "There's no job directory to change into "
        echo "Here's LOCAL TMP "
        ls -la /local/tmp
        echo "AND LOCAL TMP FRED "
        ls -la /local/tmp/yimei
        echo "Exiting"
        exit 1
fi

cp $JAR_PATH/gptest.jar .
cp -r $DATA_PATH ./data
cp -r $ALGO_PATH/params ./params
sleep 2

/usr/pkg/java/sun-8/bin/java -jar gptest.jar -file params/test.params -p train-path=$ALGO_PATH/$INSTANCE/ -p eval.problem.eval-model.instances.0.file=$DATASET"/"$INSTANCE".dat"

cp -r test/ $ALGO_PATH/$INSTANCE/
cd $ALGO_PATH/$INSTANCE/
pwd
rm -fr /local/tmp/yimei/$JOB_ID
