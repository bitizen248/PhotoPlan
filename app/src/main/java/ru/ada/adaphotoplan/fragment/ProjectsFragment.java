package ru.ada.adaphotoplan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.adapter.ProjectAdapter;
import ru.ada.adaphotoplan.obj.OpenPageEvent;

/**
 * Created by Bitizen on 28.03.17.
 */

public class ProjectsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rc = view.findViewById(R.id.project_list);
        rc.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rc.setAdapter(new ProjectAdapter(getContext()));

        View createProject = view.findViewById(R.id.create_project);
        createProject.setOnClickListener(v -> {
            EventBus.getDefault().post(new OpenPageEvent(0));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null)
            ((ProjectAdapter) ((RecyclerView) view.findViewById(R.id.project_list)).getAdapter()).loadProjects();
    }
}
