package com.neeraj.users;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessAdapter extends FragmentPagerAdapter {
    public TabAccessAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                UsersFragment usersFragment=new UsersFragment();
                return usersFragment;
            case 1:
                AddUserFragment  addUserFragment=new AddUserFragment();
                return  addUserFragment;

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Users";
            case 1:
                return "Add User";

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}