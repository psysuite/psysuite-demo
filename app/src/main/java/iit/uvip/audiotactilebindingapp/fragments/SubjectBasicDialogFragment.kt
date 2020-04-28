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
import iit.uvip.audiotactilebindingapp.utility.showToast

class SubjectBasicDialogFragment: DialogFragment()
{
    val LOG_TAG:String = SubjectBasicDialogFragment::class.java.simpleName
    private var subject: SubjectBasicData? = null

    companion object {
        fun newInstance(title: String): SubjectBasicDialogFragment {
            val frag = SubjectBasicDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_basic_subject_info, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subject = arguments?.getParcelable("subject")

        // Fetch arguments from bundle and set title
        val title       = requireArguments().getString("title", "Enter Name")

        dialog?.setTitle(title)

        if(subject != null) updateGUI(subject!!)
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
                subject = subj
                sendResult()
            }
        }

        bt_clear.setOnClickListener{
            clear()
        }

        bt_cancel.setOnClickListener{
            subject = null
            sendResult()
        }

    }

    private fun updateGUI(subj: SubjectBasicData){
        txtName.setText(subj.label)
        txtAge.setText(subj.age.toString())
        radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).id)
    }

    private fun clear(){
        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()
    }

    private fun updateSubject(): SubjectBasicData?{

        val name            = txtName.text.toString()
        val age             = txtAge.text.toString()

        if(SubjectBasicData.validate(name, age).isNotBlank()){
            showToast("Seleziona un'opzione per la durata dell'intervallo", requireContext())
            return null
        }
        var gender:Int      = -1

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
        return SubjectBasicData(name, age.toInt(), gender)
    }

    private fun sendResult() {
        if (targetFragment == null) {
            return
        }
        val intent = Intent()
        intent.putExtra(MainFragment.EVENT_SUBJECT, subject)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }
}