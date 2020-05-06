package iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import android.app.Activity
import android.content.Intent
import android.widget.CompoundButton
import iit.uvip.audiotactilebindingapp.R
import android.widget.RadioButton
import iit.uvip.audiotactilebindingapp.fragments.MainFragment
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.audiotactilebindingapp.utility.showToast
import kotlinx.android.synthetic.main.fragment_subject_info_basic.*

open class SubjectBasicDialogFragment: DialogFragment()
{
    open val LOG_TAG:String = SubjectBasicDialogFragment::class.java.simpleName
    private var subject: SubjectBasicParcel? = null

    companion object {
        fun newInstance(title: String): SubjectBasicDialogFragment {
            val frag =
                SubjectBasicDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subject = arguments?.getParcelable("subject")

        if(subject == null)
            return
        // Fetch arguments from bundle and set title
        val title       = requireArguments().getString("title", "Enter Name")

        dialog?.setTitle(title)

        if(subject != null) updateGUI(subject!!)
        else                clear()

        if(subject!!.nextTrailModality == TestBasic.TEST_NEXTTRIAL_NOCHOOSE || subject!!.nextTrailModality == TestBasic.TEST_NEXTTRIAL_ANSWER)
            swInteractive.visibility    = View.INVISIBLE
            labInteractive.visibility   = View.INVISIBLE
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

        swInteractive.setOnCheckedChangeListener { _, b ->
            subject!!.nextTrailModality = when(b){
                true    -> TestBasic.TEST_NEXTTRIAL_BUTTON
                false   -> TestBasic.TEST_NEXTTRIAL_AUTO
            }
        }
    }

    protected open fun updateGUI(subj: SubjectBasicParcel){
        txtName.setText(subj.label)

        if(subj.age != -1)
            txtAge.setText(subj.age.toString())

        if(subj.gender != -1)
            radioGroupGender.check(radioGroupGender.getChildAt(subj.gender).id)

        when(subject!!.nextTrailModality){
          TestBasic.TEST_NEXTTRIAL_BUTTON   -> swInteractive.isChecked = true
          TestBasic.TEST_NEXTTRIAL_AUTO     -> swInteractive.isChecked = false
        }
    }

    protected open fun clear(){
        txtName.setText("")
        txtAge.setText("")
        radioGroupGender.clearCheck()

        if(subject!!.nextTrailModality != TestBasic.TEST_NEXTTRIAL_NOCHOOSE && subject!!.nextTrailModality != TestBasic.TEST_NEXTTRIAL_ANSWER) {
            swInteractive.isChecked     = false
            subject!!.nextTrailModality = TestBasic.TEST_NEXTTRIAL_AUTO
        }
    }

    protected open fun updateSubject(): SubjectBasicParcel?{

        val name            = txtName.text.toString()
        val age             = txtAge.text.toString()

        if(SubjectBasicParcel.validate(name, age).isNotBlank()){
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
        subject!!.label = name
        subject!!.age = age.toInt()
        subject!!.gender = gender
        return subject
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