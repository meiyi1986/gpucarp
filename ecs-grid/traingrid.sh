#$ -S /bin/sh
#$ -wd /vol/grid-solar/sgeusers/yimei
##$ -M Fred.Bloggs@ecs.vuw.ac.nz 
##$ -m be 

ALGO="reactivegp"
DATASET=$1
INSTANCEID=$2
INSTANCE=$1$2
JAR_PATH="/vol/grid-solar/sgeusers/yimei/gphhucarp/package"
DATA_PATH="/vol/grid-solar/sgeusers/yimei/gphhucarp/data"
ALGO_PATH="/vol/grid-solar/sgeusers/yimei/gphhucarp/"$ALGO


if [ ! -d $ALGO_PATH/$INSTANCE ]; then
  mkdir $ALGO_PATH/$INSTANCE
fi

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

cp $JAR_PATH/simpleevolve.jar .
cp -r $DATA_PATH ./data
cp -r $ALGO_PATH/params ./params
sleep 2

/usr/pkg/java/sun-8/bin/java -jar simpleevolve.jar -file params/train.params -p seed.0=$(($SGE_TASK_ID-1)) -p stat.file=job.$(($SGE_TASK_ID-1)).out.stat -p eval.problem.eval-model.instances.0.file=$DATASET"/"$INSTANCE".dat"

cp params/*.stat $ALGO_PATH/$INSTANCE
cp *.csv $ALGO_PATH/$INSTANCE
cd $ALGO_PATH/$INSTANCE
pwd
rm -fr /local/tmp/yimei/$JOB_ID
