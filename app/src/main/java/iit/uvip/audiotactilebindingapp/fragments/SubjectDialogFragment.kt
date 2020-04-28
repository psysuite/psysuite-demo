package iit.uvip.audiotactilebindingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import android.app.Activity
import android.content.Intent
import iit.uvip.audiotactilebindingapp.R
import kotlinx.android.synthetic.main.fragment_tid_subject_info.*
import android.widget.RadioButton
import iit.uvip.audiotactilebindingapp.subjects.SubjectBasicData
import iit.uvip.audiotactilebindingapp.subjects.SubjectTIDData
import iit.uvip.audiotactilebindingapp.utility.showToast

class SubjectDialogFragment: DialogFragment()
{
    val LOG_TAG:String = SubjectDialogFragment::class.java.simpleName
    private var subjectTID: SubjectTIDData? = null

    companion object {
        fun newInstance(title: String): SubjectDialogFragment {
            val frag = SubjectDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tid_subject_info, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subjectTID = arguments?.getParcelable("subject")

        // Fetch arguments from bundle and set title
        val title       = requireArguments().getString("title", "Enter Name")

        dialog?.setTitle(title)

        if(subjectTID != null) updateGUI(subjectTID!!)
        else                clear()
    }

    override fun onResume() {

        // Get existing layout params for the window
        val params = dialog?.window!!.attributes
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as WindowManager.LayoutParams

        super.onResume()

        bt_confirm.setOnClickListener{

            val subj = updateSubject()
            if(subj != null) {
                subjectTID = subj
                sendResult()
            }
        }

        bt_clear.setOnClickListener{
            clear()
        }

        bt_cancel.setOnClickListener{
            subjectTID = null
            sendResult()
        }

    }

    private fun updateGUI(subj: SubjectTIDData){

        txtName.setText(subj.label)
        txtAge.setText(subj.age.toString())

        radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).getId())
        radioGroupIntervals.check(radioGroupIntervals.getChildAt(subj.interval_type).getId())
        radioGroupModality.check(radioGroupModality.getChildAt(subj.modality).getId())
        radioGroupFirstModality.check(radioGroupFirstModality.getChildAt(subj.modality).getId())
    }

    private fun clear(){
        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()
        radioGroupIntervals.clearCheck()
        radioGroupModality.clearCheck()
        radioGroupFirstModality.clearCheck()
    }

    private fun updateSubject(): SubjectTIDData?{

        val name            = txtName.text.toString()
        val age             = txtAge.text.toString()

        if(SubjectBasicData.validate(name, age).isNotBlank()){
            showToast("Seleziona un'opzione per la durata dell'intervallo", requireContext())
            return null
        }

        var first_modality:Int    = -1
        var modality:Int    = -1
        var interval:Int    = -1
        var gender:Int      = -1

        when(radioGroupIntervals.checkedRadioButtonId != -1) {
            true -> {
                val id                      = radioGroupIntervals.checkedRadioButtonId
                val radioButton:RadioButton = radioGroupIntervals.findViewById(id)
                interval                    = radioGroupIntervals.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per la durata dell'intervallo", requireContext())
                return null
            }
        }

        when(radioGroupModality.checkedRadioButtonId != -1) {
            true -> {
                val id                      = radioGroupModality.checkedRadioButtonId
                val radioButton:RadioButton = radioGroupModality.findViewById(id)
                modality                    = radioGroupModality.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per la modalità di training", requireContext())
                return null
            }
        }

        when(radioGroupFirstModality.checkedRadioButtonId != -1) {
            true -> {
                val id                      = radioGroupFirstModality.checkedRadioButtonId
                val radioButton:RadioButton = radioGroupFirstModality.findViewById(id)
                first_modality              = radioGroupFirstModality.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per la modalità di training", requireContext())
                return null
            }
        }

        when(radioGroupGender.checkedRadioButtonId != -1) {
            true -> {
                val id                      = radioGroupGender.checkedRadioButtonId
                val radioButton:RadioButton = radioGroupGender.findViewById(id)
                gender                      = radioGroupGender.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton
            }
            false -> {
                showToast("Seleziona un'opzione per il sesso", requireContext())
                return null
            }
        }
        return SubjectTIDData(name, age.toInt(), gender, modality, interval, first_modality)
    }

    private fun sendResult() {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()
        intent.putExtra(MainFragment.EVENT_SUBJECT, subjectTID)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }
}