package qqslidemenu.chinasoft.com.qqslidemenu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private ListView menu_listView, main_listView;
    private SlideMenu slideMenu;
    private ImageView iv_head;
    private MyLinearLayout my_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        my_layout.setSlideMenu(slideMenu);
        slideMenu.setOnDragListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                Log.e("setOnDragListener", "onOpen");
                menu_listView.smoothScrollToPosition(new Random().nextInt(menu_listView.getCount()));
            }

            @Override
            public void onClose() {
                Log.e("setOnDragListener", "onClose");
                ViewPropertyAnimator.animate(iv_head)
                        .translationX(15)
                        .setInterpolator(new CycleInterpolator(4))
                        .setDuration(500)
                        .start();
            }

            @Override
            public void onDraging(float fraction) {
                Log.e("setOnDragListener", "onDraging");
                ViewHelper.setAlpha(iv_head, 1-fraction);
            }
        });
        menu_listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        main_listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAMES) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView == null ? super.getView(position, convertView, parent) : convertView;
                ViewHelper.setScaleX(view, 0.5f);
                ViewHelper.setScaleY(view, 0.5f);
                //属性动画的形式还原
                ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350);
                ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350);
                return view;
            }
        });
    }

    private void initView() {
        my_layout = (MyLinearLayout) findViewById(R.id.my_layout);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        menu_listView = (ListView) findViewById(R.id.menu_listview);
        main_listView = (ListView) findViewById(R.id.main_listview);
    }
}
