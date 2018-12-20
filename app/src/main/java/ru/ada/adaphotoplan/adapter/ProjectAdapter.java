package ru.ada.adaphotoplan.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.activity.ProjectActivity;
import ru.ada.adaphotoplan.helpers.DrawHelper;
import ru.ada.adaphotoplan.helpers.ProjectFactory;
import ru.ada.adaphotoplan.helpers.ShareHelper;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;

/**
 * Created by Bitizen on 16.06.17.
 */

public class ProjectAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ProjectAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SPACE = 1;

    private Context context;
    private List<PhotoPlanProject> projects = new ArrayList<>();

    public ProjectAdapter(Context context) {
        this.context = context;
        loadProjects();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater lf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEM)
            return new ProjectViewHolder(lf.inflate(R.layout.item_project, parent, false));
        else if (viewType == TYPE_SPACE)
            return new SimpleViewHolder(lf.inflate(R.layout.item_space, parent, false));
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < projects.size()) {
            PhotoPlanProject project = projects.get(position);
            ProjectViewHolder pHolder = (ProjectViewHolder) holder;

            int width = context.getResources().getDisplayMetrics().widthPixels;
            int height = context.getResources().getDisplayMetrics().heightPixels / 3;

            Single
                    .create(e -> {
                        Bitmap bitmap =
                                DrawHelper
                                        .renderThumbnail(context, width, height, project.getId());
                        if (bitmap != null)
                            e.onSuccess(bitmap);
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(b -> pHolder.preview.setImageBitmap((Bitmap) b))
                    .subscribe();

            pHolder.name.setText(project.getName());
            pHolder.preview.setOnClickListener(view -> {
                Intent intent = new Intent(context, ProjectActivity.class);
                intent.putExtra(ProjectActivity.PROJECT_ID, project.getId());
                context.startActivity(intent);
            });

            pHolder.edit.setOnClickListener(view -> {
                Intent intent = new Intent(context, ProjectActivity.class);
                intent.putExtra(ProjectActivity.PROJECT_ID, project.getId());
                context.startActivity(intent);
            });

            pHolder.share.setOnClickListener(view -> {
                ShareHelper
                        .renderAndOutputImage(context, project)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(b -> Toast.makeText(context, b ?
                                R.string.share_success :
                                R.string.share_fail,
                                Toast.LENGTH_SHORT).show())
                        .subscribe();
            });

            pHolder.copy.setOnClickListener(view -> {
                try {
                    ProjectFactory.copyProject(context, project.getId());
                    loadProjects();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            pHolder.delete.setOnClickListener(view -> {
                ProjectFactory.deleteProject(project.getId());
                projects.remove(holder.getLayoutPosition());
                notifyItemRemoved(holder.getLayoutPosition());
            });

        }

    }

    public void loadProjects() {
        projects.clear();
        ProjectFactory
                .getProjects()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .sorted((p1, p2) -> (int) (p2.getLastOpen() - p1.getLastOpen()))
                .doOnNext(p -> projects.add(p))
                .doOnComplete(this::notifyDataSetChanged)
                .subscribe();
    }

    @Override
    public int getItemCount() {
        return projects.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1)
            return TYPE_SPACE;
        else
            return TYPE_ITEM;
    }

    private class ProjectViewHolder extends RecyclerView.ViewHolder{

        ImageView preview;
        TextView name;
        View edit;
        View share;
        View copy;
        View delete;

        ProjectViewHolder(View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.preview);
            name = itemView.findViewById(R.id.name);
            edit = itemView.findViewById(R.id.edit);
            share = itemView.findViewById(R.id.share);
            copy = itemView.findViewById(R.id.copy);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {

        SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }


}
