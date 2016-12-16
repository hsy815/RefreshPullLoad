package com.hsy.refershpullload;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.hsy.refershloading.adapter.RecyclerAdapter;
import com.hsy.refershloading.view.MyRecyclerView;
import com.hsy.refershloading.view.PullDownLoadView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyRecyclerView recyclerView;
    private PullDownLoadView pullDownLoadView;
    private RecyclerAdapter viewAdapter;

    private int a = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
    }

    private void initview() {
        recyclerView = (MyRecyclerView) findViewById(R.id.recycler);
        pullDownLoadView = (PullDownLoadView) findViewById(R.id.pullDownLoadView);
        pullDownLoadView.setMoveDistanceTop(pullDownLoadView.MoveDistanceAll);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        viewAdapter = new RecyclerAdapter(this, getData());
        recyclerView.setAdapter(viewAdapter);
        pullDownLoadView.setOnRefreshListener(new PullDownLoadView.onRefreshListener() {
            @Override
            public void onRefresh() {
                a = 1;
                timer.start();
            }

            @Override
            public void onLoadMore() {
                a = 2;
                timer.start();
            }
        });
    }

    private CountDownTimer timer = new CountDownTimer(3000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            switch (a) {
                case 1:
                    pullDownLoadView.stopRefresh();
                    break;
                case 2:
                    pullDownLoadView.stopLoadMore();
                    break;
            }
        }
    };

    public List<String> getData() {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            list.add("item" + i);
        }
        return list;
    }
}
