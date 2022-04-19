package io.github.hengyunabc.zabbix.sender;

/**
 *
 * @author hengyunabc
 *
 */
public class SenderResult {
    private int processed;
    private int failed;
    private int total;

    private float spentSeconds;

    /**
     * sometimes zabbix server will return "[]".
     */
    boolean bReturnEmptyArray = false;

    /**
     * if all sent data are processed, will return true, else return false.
     */
    public boolean success()
    {
        return !bReturnEmptyArray && processed == total;
    }

    public int getProcessed()
    {
        return processed;
    }

    public void setProcessed(int processed)
    {
        this.processed = processed;
    }

    public int getFailed()
    {
        return failed;
    }

    public void setFailed(int failed)
    {
        this.failed = failed;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public float getSpentSeconds()
    {
        return spentSeconds;
    }

    public void setSpentSeconds(float spentSeconds)
    {
        this.spentSeconds = spentSeconds;
    }

    public void setbReturnEmptyArray(boolean bReturnEmptyArray)
    {
        this.bReturnEmptyArray = bReturnEmptyArray;
    }

    @Override
    public String toString()
    {
        return "\"processed\":" + processed + ",\failed\":" + failed + ",\"total\":" + total + ",\"spentSeconds\"" + spentSeconds;
    }
}
