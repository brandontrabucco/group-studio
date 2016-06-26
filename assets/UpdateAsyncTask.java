package com.brandon.apps.groupstudio.assets;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.brandon.apps.groupstudio.models.DataModel;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.MemberModel;
import com.brandon.apps.groupstudio.models.TypeModel;
import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Brandon on 11/26/2015.
 */
public class UpdateAsyncTask <T> extends AsyncTask<Void, Void, List<T>> {
    private Context context;
    public DatabaseAdapter database;
    public List<T> UpdateTask() { return null; }
    public void UpdateView(List<T> result) { }
    public UpdateAsyncTask(Context c) {
        context = c;
        database = new DatabaseAdapter(context);
        database.open();
    }
    @Override
    protected List<T> doInBackground(Void... data) {
        return UpdateTask();
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(List<T> result) {
        System.out.println(result.toString());
        UpdateView(result);
        database.close();
    }
}
