package com.brandon.apps.groupstudio.assets;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.brandon.apps.groupstudio.models.DataModel;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.TargetModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Brandon on 11/26/2015.
 */
public class GetAllAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    private DatabaseAdapter database;
    public List<GroupModel> modelList;
    public GetAllAsyncTask(Context c) {
        modelList = new ArrayList<GroupModel>();
        context = c;
        database = new DatabaseAdapter(context);
    }
    public void OnListUpdate() {}
    @Override
    protected String doInBackground(Void... data) {
        database.open();
        String ip = database.selectServerIp();
        String url;
        if (ip.equals("")) {
            url = "";
        } else {
            url = (ip.trim().substring(ip.length() - 1).equals("/")) ? ip + "GetAllGroupData" : ip + "/GetAllGroupData";
        }
        String password = database.selectServerPassword();
        database.close();

        Gson serializer = new Gson();
        String json = serializer.toJson(new TargetModel(password, null));

        return WebConnectionHandler.post(url, json);
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        if(!result.equals("")) {
            try {
                switch (Integer.parseInt(result)) {
                    case (ResponseCode.Success):
                        // Should not ever output this
                        break;
                    case (ResponseCode.NotAuthorized):
                        Toast.makeText(context, "Not authorized, check password!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(context, "Server error!", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (NumberFormatException e) {
                Gson gson = new Gson();
                modelList = gson.fromJson(result, new TypeToken<ArrayList<GroupModel>>(){}.getType());
                OnListUpdate();
            }
        } else {
            Toast.makeText(context, "Error, check network or server settings!", Toast.LENGTH_SHORT).show();
        }
    }
}
