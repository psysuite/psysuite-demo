package iit.uvip.audiotactilebindingapp.tests.tid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import android.app.Activity
import android.content.Intent
import android.widget.ArrayAdapter
import iit.uvip.audiotactilebindingapp.R
import kotlinx.android.synthetic.main.fragment_subject_info_tid.*
import android.widget.RadioButton
import iit.uvip.audiotactilebindingapp.fragments.MainFragment
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.audiotactilebindingapp.utility.showToast

class SubjectTIDDialogFragment: DialogFragment()
{
    val LOG_TAG:String = SubjectTIDDialogFragment::class.java.simpleName
    private var subject: SubjectTIDParcel? = null

    private var nAllowedSessions:Int = 0

    companion object {
        fun newInstance(title: String): SubjectTIDDialogFragment {
            val frag        =
                SubjectTIDDialogFragment()
            val args        = Bundle()
            args.putString("title", title)
            frag.arguments = args

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_tid, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(requireContext(), R.array.tid_sessions_array, android.R.layout.simple_spinner_item)
        .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSession.adapter = adapter
            nAllowedSessions = adapter.count
        }
        subject = arguments?.getParcelable("subject")
        // Fetch arguments from bundle and set title
        val title       = requireArguments().getString("title", "Enter Name")

        dialog?.setTitle(title)

        if(subject != null) updateGUI(subject!!)
        else                clear()
    }

    override fun onResume() {

        // Get existing layout params for the window
        val params      = dialog?.window!!.attributes
        // Assign window properties to fill the parent
        params.width    = WindowManager.LayoutParams.MATCH_PARENT
        params.height   = WindowManager.LayoutParams.MATCH_PARENT
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

    private fun updateGUI(subj: SubjectTIDParcel){

        txtName.setText(subj.label)
        txtAge.setText(subj.age.toString())

        radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).id)
        radioGroupIntervals.check(radioGroupIntervals.getChildAt(subj.interval_type).id)
        radioGroupModality.check(radioGroupModality.getChildAt(subj.modality).id)
        radioGroupFirstModality.check(radioGroupFirstModality.getChildAt(subj.modality).id)
        spSession.setSelection(subj.session)
    }

    private fun clear(){
        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()
        radioGroupIntervals.clearCheck()
        radioGroupModality.clearCheck()
        radioGroupFirstModality.clearCheck()
        spSession.setSelection(nAllowedSessions-1)
    }

    private fun updateSubject(): SubjectTIDParcel?{

        val name            = txtName.text.toString()
        val age             = txtAge.text.toString()

        if(SubjectBasicParcel.validate(name, age).isNotBlank()){
            showToast("Seleziona un'opzione per la durata dell'intervallo", requireContext())
            return null
        }

        val session:Int         = spSession.selectedItemId.toInt()
        var first_modality:Int  = -1
        var modality:Int        = -1
        var interval:Int        = -1
        var gender:Int          = -1

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
        return SubjectTIDParcel(
            name,
            age.toInt(),
            gender,
            session,
            modality,
            interval,
            first_modality
        )
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