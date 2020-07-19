package com.example.project2.ui.phonebook;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2.JsonTaskGet;
import com.example.project2.JsonTaskPost;
import com.example.project2.R;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PhoneBookFragment extends Fragment {
    protected static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    protected static final int PERMISSIONS_REQUEST_SEND_SMS = 2;
    protected static final int PERMISSIONS_CALL_PHONE = 3;
    protected static final int PERMISSIONS_REQUEST_ALL = 4;
    private static String[] requiredPermissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
    };

    private PhoneBookAdapter adapter;
    private LinearLayoutManager layoutManager;

    private ListView listview;
    private ArrayAdapter searchAdapter;
    private SearchView searchView;
    private ArrayList<JsonData> inAppContact;
    private ArrayList<JsonData> serverContact;

    public ArrayList<JsonData> getServerContact() {
        return serverContact;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        requestRequiredPermissions();
        View root = inflater.inflate(R.layout.fragment_phonebook, container, false);
        adapter = new PhoneBookAdapter(new ArrayList<JsonData>(), getContext());


        String body = "";
        ContactRepository repository = new ContactRepository(this.getContext());
        inAppContact = repository.getContactList();
        serverContact= new ArrayList<>();
        new JsonTaskGetPhone().execute("http://192.249.19.244:1180/phonebook");

        RecyclerView recyclerView = root.findViewById(R.id.pb_recycler_view);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        for (int i = 0; i < inAppContact.size(); i++) {
            String name = inAppContact.get(i).getName();
            String number = inAppContact.get(i).getNumber();
            String photoid = inAppContact.get(i).getPhoto().toString();
//            String id = String.valueOf(Profile.getCurrentProfile().getId());
            String id = "123";

            body = "id=" + id + '&' + "name=" + name + '&' + "number=" + number + '&' + "photoid=" + photoid;
            new JsonTaskPost().execute("http://192.249.19.244:1180/phonebook", body);

        }


        initializeContacts();
        setHasOptionsMenu(true); // For option menu
        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS:
            case PERMISSIONS_REQUEST_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    initializeContacts();
        }
    }

    private void initializeContacts() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            adapter.updateItems(serverContact);//serverContact로 바꿔야함.
            adapter.notifyDataSetChanged();
        }
    }

    private void requestRequiredPermissions() {
        boolean allGranted = true;
        for (String permission : PhoneBookFragment.requiredPermissions) {
            boolean granted = ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
            allGranted = allGranted && granted;
        }

        if (!allGranted)
            requestPermissions(requiredPermissions, PERMISSIONS_REQUEST_ALL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        adapter.notifyDataSetChanged();
        inflater.inflate(R.menu.top_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);

        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // listview.setAdapter(searchAdapter);
                //adapter.fillter(query);
                Log.d("submitted: ", query);
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //TODO: 필터 관련 소스 Filterable 인터페이스를 Adapter 클래스에 구현하자.
                // Here is where we are going to implement the filter logic

                if (newText.length() > 0) {
                    adapter.fillter(newText, serverContact); // 필터를 통해서 현재 보여주는 값 수정함.
                    adapter.notifyDataSetChanged();
                    //TODO: 현재 검색이 안될 경우 clear를 통해 초기화 됌. 최종으로 축소되었을때 backup

                } else {

                }
                return true;
            }

        });

        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {

                //adapter.getListViewItemList().clear();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                adapter.getListViewItemList().clear();
                adapter.getListViewItemList().addAll(serverContact);
                adapter.notifyDataSetChanged();

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    public void jsonParsing(String json) throws JSONException {

            JSONArray jarray = new JSONArray(json);
            //  ArrayList<JsonData> datalist = new ArrayList<>();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String id = jObject.getString("id");
                String name = jObject.getString("name");
                String number = jObject.getString("number");
                JsonData data = new JsonData(name, number, id, id);
                serverContact.add(data);;
                adapter.notifyDataSetChanged();
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

    }


    public class JsonTaskGetPhone extends AsyncTask<String, String, String> {
        ProgressDialog dialog;
        protected void onPreExecute() {

            super.onPreExecute();


            dialog = new ProgressDialog(getContext());

            //dialog.setCancelable(false);

            dialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {

            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();


                    con.connect();

                    InputStream stream = con.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //실제 데이터를 받는곳
                    StringBuffer buffer = new StringBuffer();
                    //line별 스트링을 받기 위한 temp 변수
                    String line = "";
                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String… urls) 니까
                    return buffer.toString();
                    //아래는 예외처리 부분이다.




                } catch (MalformedURLException e){

                    e.printStackTrace();

                } catch (IOException e) {

                    e.printStackTrace();

                } finally {

                    if(con != null){

                        con.disconnect();

                    }

                    try {

                        if(reader != null){

                            reader.close();//버퍼를 닫아줌

                        }

                    } catch (IOException e) {

                        e.printStackTrace();

                    }

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

            return null;

        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dialog.dismiss();
            //Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            adapter.getListViewItemList().clear();
            try {
                JSONArray jarray = new JSONArray(result);
                //  ArrayList<JsonData> datalist = new ArrayList<>();
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                    String id = jObject.getString("id");
                    String name = jObject.getString("name");
                    String number = jObject.getString("number");
                    JsonData data = new JsonData(name, number, id, id);
                    serverContact.add(data);
                }


            }catch (JSONException e) {

                e.printStackTrace();

            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.updateItems(serverContact);
                    adapter.notifyDataSetChanged();
                }
            });
            Log.d("printget",result);
        }

    }


}
