package com.phungnlg.hellodoctor;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Phil on 7/3/2017.
 */
@EActivity(R.layout.activity_service_main)
public class ServiceActivity extends AppCompatActivity {
    @ViewById(R.id.activity_service_main_list)
    protected RecyclerView listService;

    private LinearLayoutManager layoutManager;

    @AfterViews
    public void init() {
        FastItemAdapter fastAdapter = new FastItemAdapter();
        ServiceDashboardItem itemExclusive = new ServiceDashboardItem();
        itemExclusive.setName("Độc quyền");
        itemExclusive.setDetail("Xem chi tiết");
        //itemExclusive.setDrawable(R.drawable.bg_service1);
        fastAdapter.add(itemExclusive);

        ServiceDashboardItem itemMedicare = new ServiceDashboardItem();
        itemMedicare.setName("Dịch vụ y tế");
        itemMedicare.setDetail("Xem chi tiết");
        ///itemMedicare.setDrawable(R.drawable.bg_service2);
        fastAdapter.add(itemMedicare);

        ServiceDashboardItem itemTest = new ServiceDashboardItem();
        itemTest.setName("Xét nghiệm");
        itemTest.setDetail("Xem chi tiết");
        //itemTest.setDrawable(R.drawable.bg_service3);
        fastAdapter.add(itemTest);


        layoutManager = new LinearLayoutManager(this);

        listService.setFocusable(false);
        listService.setHasFixedSize(true);
        listService.setNestedScrollingEnabled(false);
        listService.setLayoutManager(layoutManager);
        listService.setAdapter(fastAdapter);


    }
}
