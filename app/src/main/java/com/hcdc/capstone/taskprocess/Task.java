package com.hcdc.capstone.taskprocess;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hcdc.capstone.BaseActivity;
import com.hcdc.capstone.Homepage;
import com.hcdc.capstone.R;
import com.hcdc.capstone.transactionprocess.Transaction;
import com.hcdc.capstone.rewardprocess.Reward;

public class Task extends BaseActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private final String[] tabTitles = new String[]{"Tasks", "My Task"};

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new TabPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])).attach();

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks
                // ...
                int itemId = item.getItemId();

                if (itemId == R.id.action_home) {
                    navigateToActivity(Homepage.class);
                    return true;

                } else if (itemId == R.id.action_task) {
                    navigateToActivity(Task.class);
                    return true;

                } else if (itemId == R.id.action_reward) {
                    navigateToActivity(Reward.class);
                    return true;

                } else if (itemId == R.id.action_transaction) {
                    navigateToActivity(Transaction.class);
                    return true;
                }

                return false;
            }
        });
        boolean navigateToMyTasks = getIntent().getBooleanExtra("navigateToMyTasks", false);
        if (navigateToMyTasks) {
            viewPager.setCurrentItem(1); // Select the My Task tab
        }

    }

    private class TabPagerAdapter extends FragmentStateAdapter {

        public TabPagerAdapter(Task activity) {
            super(activity);
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new taskFragment();
                case 1:
                    return new userTaskFragment();
                default:
                    return null;
            }
        }
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
