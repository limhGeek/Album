package com.limh.album

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var context: Context
    private var imgUri: Uri? = null

    //相机请求ID
    private val REQUEST_CAMERA = 100
    //相册请求ID
    private val REQUEST_ALBUM = 101
    //裁剪请求ID
    private val REQUEST_CORP = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = WeakReference<Context>(this).get()!!
        btnAlbum.setOnClickListener { openAlbum() }
        btnCamera.setOnClickListener { openCamera() }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    /**
     * 打开相机
     */
    private fun openCamera() {
        Log.d(TAG, "打开相机")
        imgUri = FileUtils.getFileUri(context, "CAMERA_${System.currentTimeMillis()}.jpg")
        // 创建Intent，用于启动手机的照相机拍照
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 指定输出到文件uri中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
        // 启动intent开始拍照
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    /**
     * 打开相册
     */
    private fun openAlbum() {
        Log.d(TAG, "打开相册")
        val albumIntent = Intent(Intent.ACTION_PICK)
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(albumIntent, REQUEST_ALBUM)
    }

    /**
     * 裁剪图片
     * @param uri:要裁剪的文件
     */
    private fun corpPic(uri: Uri?) {
        //裁剪后的文件名称
        val fileName = "Crop_${System.currentTimeMillis()}.jpg"
        //裁剪后文件Uri
        imgUri = FileUtils.getFileUri(context, fileName)
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        //以下两行添加，解决无法加载此图片的提示
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(FileUtils.getFilePath(fileName))))
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1) // 裁剪框比例
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", 100) // 输出图片大小
        intent.putExtra("outputY", 100)
        intent.putExtra("scale", false)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        startActivityForResult(intent, REQUEST_CORP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "返回结果：${data ?: data.toString()}")
        if (requestCode == REQUEST_CAMERA) {
            //相机结果返回
            if (resultCode == Activity.RESULT_OK) {
                imgUri?.let { corpPic(it) }
            }
        } else if (requestCode == REQUEST_ALBUM) {
            //相册返回结果
            if (resultCode == Activity.RESULT_OK) {
                //选择的图片转存 在DCIM/Image目录
                data?.let { corpPic(it.data) }
            }
        } else if (requestCode == REQUEST_CORP) {
            Picasso.get().load(imgUri).memoryPolicy(MemoryPolicy.NO_CACHE).into(image)
        }
    }

    /**
     * 获取权限
     * 因为是一次请求完几个权限，不是在用到的时候再请求，
     * 所以不需要重写onRequestPermissionsResult方法
     */
    private fun checkPermission() {
        val storage = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val camera = arrayOf(Manifest.permission.CAMERA)
        //检查相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 之前拒绝了权限，但没有点击 不再询问 这个时候让它继续请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Log.d(TAG, "用户曾拒绝打开相机权限")
                ActivityCompat.requestPermissions(this, camera, 100)
            } else {
                //注册相机权限
                ActivityCompat.requestPermissions(this, camera, 100)
            }
        }
        //检查文件读写权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Log.d(TAG, "用户曾拒绝打开文件读写权限")
                ActivityCompat.requestPermissions(this, storage, 101)
            } else {
                //注册相机权限
                ActivityCompat.requestPermissions(this, storage, 101)
            }
        }
    }

}
