package com.alim.cse.noticebynu.Fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.alim.cse.noticebynu.Adapter.Updates;
import com.alim.cse.noticebynu.Config.Final;
import com.alim.cse.noticebynu.Process.Compressor;
import com.alim.cse.noticebynu.Process.UIProcess;
import com.alim.cse.noticebynu.R;
import com.alim.cse.noticebynu.Services.PushData;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdatesFragment extends Fragment{

    int start, end;
    String WebData;
    ImageView menu;
    Boolean scroll = false;
    ProgressBar progressBar;
    FloatingActionButton top;
    private RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView.Adapter mAdapter;
    static List<String> mData = new ArrayList<>();
    static List<String> mDate = new ArrayList<>();
    static List<String> mLink = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        shimmerFrameLayout = rootView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        top = rootView.findViewById(R.id.go_top);
        menu = rootView.findViewById(R.id.menu);
        progressBar = rootView.findViewById(R.id.progress);
        recyclerView = rootView.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new Updates(mData, mDate, mLink, false,"Notice");
        recyclerView.setVisibility(View.GONE);
        recyclerView.setAdapter(mAdapter);
        if (mData.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);
            final long FIVE_MEGABYTE = 1024 * 1024*3;
            StorageReference mountainsRef = storageRef.child("Updates.zip");
            mountainsRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    SimpleDateFormat formatter = new SimpleDateFormat("HH");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(storageMetadata.getCreationTimeMillis());
                    int date =  Integer.parseInt(formatter.format(calendar.getTime()));
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH");
                    int date_N = Integer.parseInt(sdf.format(currentTime));
                    if (date-date_N>4 | date_N-date>4) {
                        new PushData(getActivity()).new ParseURL().execute(Final.LINK(), "Updates.zip");
                        Log.println(Log.ASSERT,"DATE","GOT IT");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    new PushData(getActivity()).new ParseURL().execute(Final.LINK(), "Updates.zip");
                }
            });
            progressBar.setProgress(20);
            mountainsRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    String s = new String(bytes);
                    Log.println(Log.ASSERT,"BYTE",s);
                    new GetArray().execute(new Compressor(bytes).Unzip());
                    progressBar.setProgress(70);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.println(Log.ASSERT,"FAILED", exception.toString());
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else
            Shimmer();

        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.smoothScrollToPosition(0);
                scroll = true;
                top.hide();
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.about:
                                new UIProcess(getActivity()).About();
                                break;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.top_menu);
                popup.show();
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (scroll & dy>0)
                    scroll = false;
                if (dy<0 & !scroll)
                    top.show();
                else
                    top.hide();
            }
        });

        return rootView;
    }

    private class GetArray extends AsyncTask<String, Integer, List<String>> {
        @Override
        protected List<String> doInBackground(String... strings) {
            WebData = strings[0];
            try {
                publishProgress(90);
                for (int x = 0; x < 200; x++) {
                    start = WebData.indexOf("<tr>");
                    end = WebData.indexOf("</tr>", start) + 5;
                    String table = WebData.substring(start, end);
                    int a = table.indexOf("href=\"uploads/")+6;
                    int b = table.indexOf(".pdf", a) + 4;
                    int c = table.indexOf("title=") + 7;
                    int d = table.indexOf("\">", c);
                    int e = table.lastIndexOf("solid;\">") + 8;
                    int f = table.lastIndexOf("</td>");
                    if (a < b & c < d & e < f) {
                        mLink.add("http://www.nu.ac.bd/" + table.substring(a, b));
                        mData.add(table.substring(c, d));
                        mDate.add(table.substring(e, f));
                        //myRef.child("Notice").child(position).child("Title").setValue(table.substring(c,d));
                        //myRef.child("Notice").child(position).child("Date").setValue(table.substring(e,f));
                        //myRef.child("Notice").child(position).child("Link").setValue(table.substring(a,b));
                    } else if (a > b) {
                        b = table.indexOf(".txt", a)+4;
                        Log.println(Log.ASSERT, "Value", a + "-" + b + "-" + c + "-" + d + "-" + e + "-" + f);
                        if (a < b) {
                            mLink.add("http://www.nu.ac.bd/" + table.substring(a, b));
                            mData.add(table.substring(c, d));
                            mDate.add(table.substring(e, f));
                        }
                    } else {
                        mData.add("Error");
                        mDate.add("Error");
                        mLink.add("Error");
                    }
                    WebData = WebData.substring(end);
                }
                publishProgress(100);
                return mData;
            } catch (Exception e) {
                Log.println(Log.ASSERT, "ERROR", e.toString());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(List<String> response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    Shimmer();
                    Log.println(Log.ASSERT, "Response", response.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.println(Log.ASSERT, "JSONException", e.toString());
                }
            } else {
                SetError();
            }
        }
    }

    private void Shimmer() {
        shimmerFrameLayout.stopShimmer();
        progressBar.setVisibility(View.INVISIBLE);
        shimmerFrameLayout.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void SetError() {
        shimmerFrameLayout.stopShimmer();
        progressBar.setVisibility(View.INVISIBLE);
        shimmerFrameLayout.setVisibility(View.GONE);
    }
}