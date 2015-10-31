package ie.hocusfocus.utils;

/**
 * Created by Brian Henry
 */
public class RollingMovingMinimum extends NumberFixedLengthFifoQueue {

    private float minValue;

    public RollingMovingMinimum(Number[] initialValues) {
        super(initialValues);
        minValue = 1.0f;

    }

    public float getValue() {
        return minValue;
    }

    @Override
    public boolean add(Number newest) {
        return this.offer(newest);
    }

    @Override
    public boolean offer(Number newest) {

        boolean res = super.offer(newest);

        Float min = 1.0f;
        for(Number n : ring){
            Float f = n.floatValue();
            if(f<min)
                min = f;
        }
        minValue = min;

        return res;
    }

}