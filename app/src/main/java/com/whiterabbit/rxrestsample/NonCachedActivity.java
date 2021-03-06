package com.whiterabbit.rxrestsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.whiterabbit.rxrestsample.adapters.RepoAdapter;
import com.whiterabbit.rxrestsample.rest.GitHubClient;
import com.whiterabbit.rxrestsample.data.Repo;


import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NonCachedActivity extends AppCompatActivity {
    @Inject GitHubClient mGithubClient;
    @Bind(R.id.pending_request_progress) ProgressBar mProgress;
    @Bind(R.id.main_list) RecyclerView mList;
    private Observable<List<Repo>> mObservable;
    private RepoAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ((RxSampleApplication) getApplication()).getComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mList.setLayoutManager(layoutManager);
        mAdapter = new RepoAdapter();
        mList.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mObservable = mGithubClient.getRepos("fedepaol");

        mObservable.delay(3, TimeUnit.SECONDS) // delayed for demonstration purpouse
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread()).subscribe(l -> {
                    mProgress.setVisibility(View.INVISIBLE);
                    mAdapter.updateData(l);
                },
                e -> mProgress.setVisibility(View.INVISIBLE),
                ()-> mProgress.setVisibility(View.INVISIBLE));

        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mObservable != null) {
            mObservable.unsubscribeOn(Schedulers.computation());
        }
    }
}
