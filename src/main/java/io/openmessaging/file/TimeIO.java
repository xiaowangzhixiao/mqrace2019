package io.openmessaging.file;

import io.openmessaging.index.TimeIndex;

/**
 * TimeIO
 */
public class TimeIO {
    private TimeIndex index = new TimeIndex(10000000);
    private byte[] times = new byte[Integer.MAX_VALUE / 10];
    private int nums = 0;
    private long baseTime = 0;

    public void put(long time) {
        if (nums == 0){
            baseTime = time >>> 8 << 8 ;
            index.setBaseTime(baseTime);
            index.put(0);
        }else {
            if (time >= baseTime + 256) {
                while (time >= baseTime + 256) {
                    baseTime = baseTime + 256;
                    index.put(-1);
                }
                index.update(nums);
            }
        }

        this.times[nums] = (byte) (time & 0xff);
        nums++;
    }

    private int getOffsetFromeIndex(int indexIndex){
        if (indexIndex >= index.getNums()){
            return this.nums;
        }
        return index.getOffset(indexIndex);
    }

    public int getIndexIndex(long time){
        return index.search(time);
    }

    public int getMinIndex(int indexIndex, long time) {
        int i = 1;
        while (getOffsetFromeIndex(indexIndex+i) == -1){i++;}
        return binarySearchMin(time - index.getBaseTime(indexIndex), getOffsetFromeIndex(indexIndex), getOffsetFromeIndex(indexIndex + i), times);
    }

    public int getMaxIndex(long time) {
        int indexIndex = index.search(time);
        int i = 1;
        while (getOffsetFromeIndex(indexIndex+i) == -1){i++;}
        return binarySearchMax(time - index.getBaseTime(indexIndex) , getOffsetFromeIndex(indexIndex), getOffsetFromeIndex(indexIndex + i), times);
    }

    public static int binarySearchMin(long t,int left, int right, byte[] time) {
        while (left < right) {
            int mid = left + (right - left) / 2;
            if ((time[mid] & 0xff) < t) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    public static int binarySearchMax(long t, int left, int right, byte[] time) {
        while (left < right) {
            int mid = left + (right - left) / 2;
            if ((time[mid] & 0xff) <= t) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    public class TimeInfo {
        private int indexIndex;
        private int maxOffset;
        private int nextIndexIndex;
        private int timeIndex;

        public TimeInfo(){

        }

        public TimeInfo(int indexIndex, int timeIndex){
            reinit(indexIndex, timeIndex);
        }

        public void reinit(int indexIndex, int timeIndex){
            this.indexIndex = indexIndex;
            this.timeIndex = timeIndex;
            int i = 1;
            while (getOffsetFromeIndex(indexIndex+i) == -1) { i++; }
            this.maxOffset = getOffsetFromeIndex(indexIndex + i);
            this.nextIndexIndex = indexIndex + i;

        }

        public long getNextTime(){
            if (timeIndex >= maxOffset){
                indexIndex = nextIndexIndex;
                int i = 1;
                while (getOffsetFromeIndex(indexIndex+i) == -1) { i++; }
                this.maxOffset = getOffsetFromeIndex(indexIndex + i);
                this.nextIndexIndex = indexIndex + i;
            }
            long time = index.getBaseTime(indexIndex) + (long)(times[timeIndex] & 0xff);
            timeIndex++;
            return time;
        }

        public boolean hasNext(){
            return timeIndex < nums;
        }
    }

    public static void main(String[] args) {
       TimeIO timeIO = new TimeIO();
        for (int i = 3; i < 400; i++) {
            timeIO.put((long)i + 1567309838571L);
        }
//        timeIO.put(1);
//        timeIO.put(2);
//        timeIO.put(600);
        int indexIndex = timeIO.getIndexIndex(1567309838571L);
        int timeIndex = timeIO.getMinIndex(indexIndex, 1567309838571L);

        int maxTimeIndex = timeIO.getMaxIndex(1567309838600L);

        TimeInfo timeInfo =timeIO.new TimeInfo(indexIndex, timeIndex);

        while (timeInfo.hasNext()){
            long t = timeInfo.getNextTime();
            if (t > 1567309838600L) break;
            System.out.println(t);
        }
        System.out.println(maxTimeIndex - timeIndex);
    }

}