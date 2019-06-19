package com.example.jonathangalvan.mirelex.Fragments.Product

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jonathangalvan.mirelex.R
import kotlinx.android.synthetic.main.fragment_product_image_picker.*
import android.content.Intent
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import java.io.FileNotFoundException
import android.provider.MediaStore.MediaColumns
import android.support.v4.app.FragmentManager
import android.widget.ImageView
import com.example.jonathangalvan.mirelex.Fragments.Utils.CustomBottomAlert
import com.example.jonathangalvan.mirelex.Interfaces.BottomAlertInterface
import com.example.jonathangalvan.mirelex.Interfaces.ProductImageInterface
import com.example.jonathangalvan.mirelex.Models.UtilsModel
import com.example.jonathangalvan.mirelex.ProductActivity
import com.example.jonathangalvan.mirelex.ViewModels.ProductViewModel
import com.squareup.picasso.Picasso
import java.io.File

class ProductImagePicker : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    var imageTarget: ImageView? = null
    var imageSecondaryIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_image_picker, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /*Get productObj*/
        val viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        var productObj = viewModel.productObj

        /*Validate if is update or insert for button text*/
        if(viewModel.productProcessType != "update"){
            productFinalProductProcess.text = activity?.resources?.getString(R.string.addProduct)
        }

        /*Update process only*/
        if(productObj?.productInformation?.productFeaturedImage != null && viewModel.productProcessType == "update"){
            Picasso.with(activity).load(productObj?.productInformation?.productFeaturedImage).into(productFeaturedImage)
        }

        /*Set images*/
        productFeaturedImage.setOnClickListener(View.OnClickListener {
            imageTarget = productFeaturedImage
            openImageSelection()
        })

        productSecondaryImage1.setOnClickListener(View.OnClickListener {
            imageTarget = productSecondaryImage1
            imageSecondaryIndex = 1
            openImageSelection()
        })

        productSecondaryImage2.setOnClickListener(View.OnClickListener {
            imageTarget = productSecondaryImage2
            imageSecondaryIndex = 2
            openImageSelection()
        })

        productSecondaryImage3.setOnClickListener(View.OnClickListener {
            imageTarget = productSecondaryImage3
            imageSecondaryIndex = 3
            openImageSelection()
        })

        /*Product final process event*/
        productFinalProductProcess.setOnClickListener(View.OnClickListener {
            (activity as ProductActivity).doFinalProductProcess()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1)
            if (resultCode === Activity.RESULT_OK) {
                val viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
                val selectedImage = data!!.getData()
                val filePath = getPath(selectedImage)
                val file_extn = filePath.substring(filePath.lastIndexOf(".") + 1)

                try {
                    if (file_extn == "img" || file_extn == "jpg" || file_extn == "jpeg" || file_extn == "png") {
                        imageTarget?.setImageURI(data?.data)
                        when(imageTarget){
                            productFeaturedImage -> {
                                viewModel.featuredImage = File(filePath)
                            }
                            productSecondaryImage1,
                            productSecondaryImage2,
                            productSecondaryImage3 -> {
                                setSecondaryImage(File(filePath))
                            }
                        }
                    } else {
                        //NOT IN REQUIRED FORMAT
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
    }

    fun getPath(uri: Uri): String {
        val projection = arrayOf(MediaColumns.DATA)
        val cursor = activity!!.managedQuery(uri, projection, null, null, null)
        var column_index = cursor
            .getColumnIndexOrThrow(MediaColumns.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    fun setSecondaryImage(file: File){
        val viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        var existsInArray = false
        for ((index, img) in viewModel.secondaryImgs.withIndex()){
            if(img.position == imageSecondaryIndex){
                existsInArray = true
                viewModel.secondaryImgs[index] = ProductImageInterface(file, imageSecondaryIndex)
            }
        }

        if(!existsInArray){
            viewModel.secondaryImgs.add(ProductImageInterface(file, imageSecondaryIndex))
        }
    }

    fun openImageSelection(){
        val ba = UtilsModel.getGson().toJson(BottomAlertInterface(
            alertType = "imageSelectOptions"
        ))
        val alert = CustomBottomAlert().bottomSheetDialogInstance(ba)
        alert.setTargetFragment(this, 1337)
        alert.show(activity!!.supportFragmentManager, "alert")
    }

    fun openGalleryPicker(){
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, 1)
    }

    fun reseatImage(){
        val viewModel = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        Picasso.with(imageTarget!!.context).load(R.drawable.image_picker).into(imageTarget)
        when(imageTarget){
            productFeaturedImage -> {
                viewModel.featuredImage = null
            }
            productSecondaryImage1,
            productSecondaryImage2,
            productSecondaryImage3 -> {
                var newObjArr = ArrayList<ProductImageInterface>()
                for (img in viewModel.secondaryImgs){
                    if(img.position != imageSecondaryIndex){
                        newObjArr.add(img)
                    }
                }
                viewModel.secondaryImgs = newObjArr
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ProductImagePicker().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
