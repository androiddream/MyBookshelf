package com.kunfei.bookshelf.view.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.help.ReadBookControl;
import com.kunfei.bookshelf.help.RxBusTag;
import com.kunfei.bookshelf.utils.BitmapUtil;
import com.kunfei.bookshelf.utils.ColorUtil;
import com.kunfei.bookshelf.utils.FileUtil;
import com.kunfei.bookshelf.utils.PermissionUtils;
import com.kunfei.bookshelf.utils.barUtil.ImmersionBar;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReadStyleActivity extends MBaseActivity {
    private final int ResultSelectBg = 103;

    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.tvSelectTextColor)
    TextView tvSelectTextColor;
    @BindView(R.id.tvSelectBgColor)
    TextView tvSelectBgColor;
    @BindView(R.id.tvSelectBgImage)
    TextView tvSelectBgImage;
    @BindView(R.id.tvDefault)
    TextView tvDefault;
    @BindView(R.id.sw_darkStatusIcon)
    Switch swDarkStatusIcon;

    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private MoDialogHUD moDialogHUD;
    private int textDrawableIndex;
    private int textColor;
    private int bgColor;
    private Drawable bgDrawable;
    private int bgCustom;
    private boolean darkStatusIcon;
    private String bgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        setContentView(R.layout.activity_read_style);
        ButterKnife.bind(this);
        llContent.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0);
        this.setSupportActionBar(toolbar);
        setupActionBar();
        setTextKind(readBookControl);
        moDialogHUD = new MoDialogHUD(this);
    }

    @Override
    protected void initImmersionBar() {
        super.initImmersionBar();
        if (!isImmersionBarEnabled()) {
            mImmersionBar.statusBarDarkFont(false);
        } else if (darkStatusIcon) {
            mImmersionBar.statusBarDarkFont(true);
        } else {
            mImmersionBar.statusBarDarkFont(false);
        }
        mImmersionBar.init();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        Intent intent = getIntent();
        textDrawableIndex = intent.getIntExtra("index", 1);
        bgCustom = readBookControl.getBgCustom(textDrawableIndex);
        textColor = readBookControl.getTextColor(textDrawableIndex);
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        bgDrawable = readBookControl.getBgDrawable(textDrawableIndex, getContext(), width, height);
        bgColor = readBookControl.getBgColor(textDrawableIndex);
        darkStatusIcon = readBookControl.getDarkStatusIcon(textDrawableIndex);
        bgPath = readBookControl.getBgPath(textDrawableIndex);
        upText();
        upBg();
    }

    /**
     * 事件触发绑定
     */
    @Override
    protected void bindEvent() {
        swDarkStatusIcon.setChecked(darkStatusIcon);
        swDarkStatusIcon.setOnCheckedChangeListener((compoundButton, b) -> {
            darkStatusIcon = b;
            initImmersionBar();
        });
        //选择文字颜色
        tvSelectTextColor.setOnClickListener(view -> ColorPickerDialogBuilder
                .with(this)
                .setTitle("选择文字颜色")
                .initialColor(textColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(selectedColor -> {

                })
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    textColor = selectedColor;
                    upText();
                })
                .setNegativeButton("cancel", (dialog, which) -> {

                })
                .build()
                .show());
        tvSelectTextColor.setOnLongClickListener((View view) -> {
            moDialogHUD.showInputBox("输入文字颜色",
                    ColorUtil.intToString(textColor),
                    null,
                    inputText -> {
                try {
                    textColor = Color.parseColor(inputText);
                    upText();
                } catch (Exception e) {
                    toast("颜色值错误", ERROR);
                }
            });
            return true;
        });
        //选择背景颜色
        tvSelectBgColor.setOnClickListener(view -> ColorPickerDialogBuilder
                .with(this)
                .setTitle("选择背景颜色")
                .initialColor(bgColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(selectedColor -> {

                })
                .setPositiveButton("ok", (dialog, selectedColor, allColors) -> {
                    bgCustom = 1;
                    bgColor = selectedColor;
                    bgDrawable = new ColorDrawable(bgColor);
                    upBg();
                })
                .setNegativeButton("cancel", (dialog, which) -> {

                })
                .build()
                .show());
        tvSelectBgColor.setOnLongClickListener((View view) -> {
            moDialogHUD.showInputBox("输入背景颜色",
                    ColorUtil.intToString(bgColor),
                    null,
                    inputText -> {
                try {
                    bgColor = Color.parseColor(inputText);
                    bgDrawable = new ColorDrawable(bgColor);
                    bgCustom = 1;
                    upBg();
                } catch (Exception e) {
                    toast("颜色值错误", ERROR);
                }
            });
            return true;
        });
        //选择背景图片
        tvSelectBgImage.setOnClickListener(view -> {
            PermissionUtils.checkMorePermissions(ReadStyleActivity.this, MApplication.PerList, new PermissionUtils.PermissionCheckCallBack() {
                @Override
                public void onHasPermission() {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, ResultSelectBg);
                }

                @Override
                public void onUserHasAlreadyTurnedDown(String... permission) {
                    ReadStyleActivity.this.toast("选择背景图片需存储权限");
                }

                @Override
                public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                    PermissionUtils.requestMorePermissions(ReadStyleActivity.this, MApplication.PerList, MApplication.RESULT__PERMS);
                }
            });
        });
        //恢复默认
        tvDefault.setOnClickListener(view -> {
            bgCustom = 0;
            textColor = readBookControl.getDefaultTextColor(textDrawableIndex);
            bgDrawable = readBookControl.getDefaultBgDrawable(textDrawableIndex, this);
            upText();
            upBg();
        });
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.read_style);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read_style_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveStyle();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存配置
     */
    private void saveStyle() {
        readBookControl.setTextColor(textDrawableIndex, textColor);
        readBookControl.setBgCustom(textDrawableIndex, bgCustom);
        readBookControl.setBgColor(textDrawableIndex, bgColor);
        readBookControl.setDarkStatusIcon(textDrawableIndex, darkStatusIcon);
        if (bgCustom == 2) {
            readBookControl.setBgPath(textDrawableIndex, bgPath);
        }
        readBookControl.initTextDrawableIndex();
        RxBus.get().post(RxBusTag.UPDATE_READ, false);
        finish();
    }

    private void setTextKind(ReadBookControl readBookControl) {
        tvContent.setTextSize(readBookControl.getTextSize());
    }

    private void upText() {
        tvContent.setTextColor(textColor);
    }

    private void upBg() {
        llContent.setBackground(bgDrawable);
    }

    /**
     * 自定义背景
     */
    public void setCustomBg(Uri uri) {
        try {
            bgPath = FileUtil.getPath(this, uri);
            Resources resources = this.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            Bitmap bitmap = BitmapUtil.getFitSampleBitmap(bgPath, width, height);
            bgCustom = 2;
            bgDrawable = new BitmapDrawable(getResources(), bitmap);
            upBg();
        } catch (Exception e) {
            e.printStackTrace();
            toast(e.getMessage(), ERROR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ResultSelectBg:
                if (resultCode == RESULT_OK && null != data) {
                    setCustomBg(data.getData());
                }
                break;
        }
    }
}
