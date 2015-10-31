package ie.hocusfocus.utils;

/**
 * Created by Brian Henry
 */
public class RollingMovingMinimum extends NumberFixedLengthFifoQueue {

    public RollingMovingMinimum(Number[] initialValues) {
        super(initialValues);


    }

    public float getValue() {
        Float min = 1.0f;
        for(Number n : ring){
            Float f = n.floatValue();
            if(f<min)
                min = f;
        }
        return min;
    }

    @Override
    public boolean add(Number newest) {
        return this.offer(newest);
    }

    @Override
    public boolean offer(Number newest) {

        boolean res = super.offer(newest);

        return res;
    }

}