package com.brandon.apps.groupstudio.assets;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.TargetModel;
import com.google.gson.Gson;

/**
 * Created by Brandon on 11/26/2015.
 */
public class DeleteAsyncTask extends AsyncTask<TargetModel, Void, String> {
    private Context context;
    private DatabaseAdapter database;
    public DeleteAsyncTask(Context c) {
        context = c;
        database = new DatabaseAdapter(context);
    }
    public void OnUpdate() {}
    @Override
    protected String doInBackground(TargetModel... data) {
        database.open();
        String ip = database.selectServerIp();
        String url;
        if (ip.equals("")) {
            url = "";
        } else {
            url = (ip.trim().substring(ip.length() - 1).equals("/")) ? ip + "DeleteGroupData" : ip + "/DeleteGroupData";
        }
        database.close();

        Gson gson = new Gson();
        return WebConnectionHandler.post(url, gson.toJson(data[0]));
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        if(!result.equals("")) {
            switch (Integer.parseInt(result)) {
                case (ResponseCode.Success):
                    // Success
                    break;
                case (ResponseCode.NotAuthorized):
                    Toast.makeText(context, "Not authorized, check password!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context, "Server error!", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(context, "Error, check network or server settings!", Toast.LENGTH_SHORT).show();
        }
        OnUpdate();
    }
}
