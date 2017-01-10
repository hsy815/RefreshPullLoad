# RefreshPullLoad
类似QQ等，上拉加载下来刷新
#引用：
```
allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```
```
compile 'com.github.hsy815:RefreshPullLoad:v1.2'
```
#使用：
```
        //普通加载View
        ordinaryPDLView = (OrdinaryPDLView) findViewById(R.id.pullDownLoadView);
        
         //刷新、上拉加载监听
        ordinaryPDLView.setOnRefreshListener(new OrdinaryPDLView.onRefreshListener() {
            @Override
            public void onRefresh() {
                timer.start();
            }

            @Override
            public void onLoadMore() {
                timer.start();
            }
        });
        
```
##使用细节：
```
        //头部下拉滑动距离 默认是头部View的高度、 ordinaryPDLView.MoveDistanceAll全屏、  ordinaryPDLView.MoveDistanceIn 半屏
        ordinaryPDLView.setMoveDistanceTop(ordinaryPDLView.MoveDistanceAll);
        //底部上拉滑动距离 默认是底部View的高度、 ordinaryPDLView.MoveDistanceAll全屏、  ordinaryPDLView.MoveDistanceIn 半屏
        ordinaryPDLView.setMoveDistanceBtn(ordinaryPDLView.MoveDistanceAll);
        //添加头部拖动箭头
        ordinaryPDLView.setDrawable_h(R.mipmap.down);
        //添加底部拖动箭头
        ordinaryPDLView.setDrawable_f(R.mipmap.pulls);
        //头部加载时文本View、文本内容以及属性自己设置
        ordinaryPDLView.ordinary_pdl_tv_h.setText("正在加载");
        //底部加载时文本View、文本内容以及属性自己设置
        ordinaryPDLView.ordinary_pdl_tv_f.setText("正在加载");
        //设置加载文本本内容
        PDLSetting pdlSetting = new PDLSetting("松开刷新", "松开加载", "继续向下拉", "继续向上拉");
        ordinaryPDLView.setPdlSetting(pdlSetting);
        //头部加载时旋转的图片
        ordinaryPDLView.ordinary_pdl_load_h.setImageResource(R.mipmap.loading_dian);
        //底部加载时旋转的图片
        ordinaryPDLView.ordinary_pdl_load_f.setImageResource(R.mipmap.loading);
        //设置头部背景
        ordinaryPDLView.LayoutHeader.setBackgroundResource(R.color.bg);
        //设置底部背景
        ordinaryPDLView.LayoutFooter.setBackgroundResource(R.color.bg);
        //旋转动画
        Animation operatingAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.ordinary_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        //添加旋转动画
        ordinaryPDLView.setOperatingAnim(operatingAnim);
        
```
