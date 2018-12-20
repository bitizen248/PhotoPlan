package ru.ada.adaphotoplan.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.ada.adaphotoplan.AdaPPAplication;
import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.helpers.ProjectFactory;
import ru.ada.adaphotoplan.helpers.ShareHelper;
import ru.ada.adaphotoplan.interfaces.OnConnectionStateChanged;
import ru.ada.adaphotoplan.interfaces.OnSelectionListener;
import ru.ada.adaphotoplan.interfaces.OnTouchPreview;
import ru.ada.adaphotoplan.obj.MeterEvent;
import ru.ada.adaphotoplan.obj.PhotoPlanProject;
import ru.ada.adaphotoplan.service.BluetoothService;
import ru.ada.adaphotoplan.view.ProjectView;

/**
 * Created by Bitizen on 09.06.17.
 */

public class ProjectActivity extends ProtoActivity {
    private static final String TAG = "ProjectActivity";

    public static final String PROJECT_ID = "id";
    private ProjectView projectView;
    private ImageView reset;
    private EditText label;
    private ImageView add;
    private ImageView center;
    private ImageView undo;
    private ImageView mode;

    private ImageView leftPreview;
    private ImageView rightPreview;

    private ImageView bluetoothStatus;

    private FloatingActionButton deleteLine;

    private PhotoPlanProject project;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        Intent intent = getIntent();

        projectView = findViewById(R.id.project_view);
        reset = findViewById(R.id.reset);
        label = findViewById(R.id.label);
        add = findViewById(R.id.add);
        center = findViewById(R.id.center);
        undo = findViewById(R.id.undo);
        mode = findViewById(R.id.mode);
        deleteLine = findViewById(R.id.delete);
        bluetoothStatus = findViewById(R.id.bluetooth_status);

        bluetoothStatus.setVisibility(View.VISIBLE);
        bluetoothStatus.setOnClickListener(view -> {
            startActivity(new Intent(this, BluetoothActivity.class));
        });

        leftPreview = findViewById(R.id.left_preview);
        rightPreview = findViewById(R.id.right_preview);

        if (intent.hasExtra(PROJECT_ID)) {
            project = ProjectFactory.openProject(intent.getIntExtra(PROJECT_ID, -1), true, true);
        }
        if (project == null) {
            project = ProjectFactory.openProject(intent.getIntExtra(PROJECT_ID, -1), false, true);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage(R.string.error_project_opening)
                    .setPositiveButton("OK", null)
                    .show();
        }
        title.setText(project.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit.setVisibility(View.VISIBLE);
        View input = getLayoutInflater().inflate(R.layout.layout_editext, null);
        edit.setOnClickListener(view -> {
            EditText name = input.findViewById(R.id.edit);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage(R.string.name)
                    .setView(input)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        project.setName(name.getText().toString());
                        title.setText(project.getName());
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
        });

        projectView.setProject(project);
        deleteLine.setVisibility(View.GONE);
        deleteLine.setOnClickListener(v -> {
            projectView.deleteSelected();
        });
        projectView.setSelectionListener(new OnSelectionListener() {
            @Override
            public void selected() {
                deleteLine.setVisibility(View.VISIBLE);
            }

            @Override
            public void unSelected() {
                deleteLine.setVisibility(View.GONE);
            }
        });

        projectView.setTouchPreview(new OnTouchPreview() {
            @Override
            public void onTouch(Bitmap preview, int x, int y) {
                if (x < 300 && y < 300) {
                    leftPreview.setVisibility(View.GONE);
                    rightPreview.setVisibility(View.VISIBLE);
                } else {
                    leftPreview.setVisibility(View.VISIBLE);
                    rightPreview.setVisibility(View.GONE);
                }
                leftPreview.setImageBitmap(preview);
                rightPreview.setImageBitmap(preview);
            }

            @Override
            public void onRelease() {
                leftPreview.setVisibility(View.GONE);
                rightPreview.setVisibility(View.GONE);
            }
        });

        label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (label.getText().toString().trim().equals(""))
                    add.setVisibility(View.INVISIBLE);
                else
                    add.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        reset.setOnClickListener(v -> {
            if (!label.getText().toString().equals(""))
                label.setText("");
            else
                projectView.addLabel(null);
        });

        add.setOnClickListener(v -> {
            if (!label.getText().toString().trim().equals(""))
                if (!projectView.addLabel(label.getText().toString()))
                    Toast.makeText(this, R.string.choose_line, Toast.LENGTH_SHORT).show();
        });

        center.setOnClickListener(view -> {
            projectView.setPosition(0, 0, 1);
        });

        undo.setOnClickListener(v -> projectView.undo());

        mode.setSelected(projectView.isDrawMode());
        mode.setOnClickListener(view -> {
            projectView.setDrawMode(!projectView.isDrawMode());
            mode.setSelected(projectView.isDrawMode());
            Toast.makeText(this, projectView.isDrawMode()? R.string.mode_draw : R.string.mode_edit , Toast.LENGTH_SHORT).show();
        });

        mode.callOnClick();
    }


    @Override
    protected void onResume() {
        super.onResume();

        BluetoothService bleService =
                ((AdaPPAplication) getApplication()).getBluetoothService();
        assert bleService != null;

        bleService.setConnectionStateChanged(new OnConnectionStateChanged() {
            @Override
            public void onDeviceConnected(String name) {
                bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_connected);
                Toast.makeText(ProjectActivity.this, R.string.connected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceDisconnected() {
                if (!bleService.isBluetoothEnabled()) {
                    bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disabled);
                } else {
                    bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_enabled);
                }
                Toast.makeText(ProjectActivity.this, R.string.connection_lost, Toast.LENGTH_SHORT).show();
            }
        });

        if (!bleService.isBluetoothEnabled()) {
            bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disabled);
        } else if (bleService.isConnected()) {
            bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_connected);
        } else {
            bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_enabled);
        }
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        confirmExit();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                ShareHelper
                        .renderAndOutputImage(this, project)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(b -> Toast.makeText(this, b ?
                                        R.string.share_success :
                                        R.string.share_fail,
                                Toast.LENGTH_SHORT).show())
                        .subscribe();
                return true;
            case R.id.save:
                ProjectFactory
                        .saveProject(project);
                Toast.makeText(this, R.string.project_save, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void metricFromBle(MeterEvent event) {
        label.setText(event.getResult());
    }

    public void confirmExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(R.string.save_before_exit)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    ProjectFactory.saveProject(project);
                    finish();
                })
                .setNegativeButton(R.string.no, (dialogInterface, i) -> {
                    finish();
                });
        builder.create().show();
    }
}
