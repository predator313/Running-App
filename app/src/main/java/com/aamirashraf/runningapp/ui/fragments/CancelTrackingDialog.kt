package com.aamirashraf.runningapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.aamirashraf.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog:DialogFragment() {
    private var yesListner:(()->Unit)?=null
     fun setYesListner(listner:() ->Unit){
        yesListner=listner
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("cancel the Run??")
            .setMessage("Are you sure to cancel the current Run and delete all its data")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                yesListner?.let{ yes->
                    yes()
                }

            }
            .setNegativeButton("No"){ dialogInterface,_ ->
                dialogInterface.cancel()
            }.create()
//        dialog.show()
    }
}