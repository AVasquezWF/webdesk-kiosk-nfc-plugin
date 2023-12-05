package ACS.task;

/**
 * Created by kevin on 16/3/18.
 */
public interface TaskListener {
    void onSuccess();

    void onFailure();

    void onException();

}
