package iit.uvip.twoafctemporalquestapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import android.app.Activity
import iit.uvip.twoafctemporalquestapp.R
import iit.uvip.twoafctemporalquestapp.tests.Test
import kotlinx.android.synthetic.main.fragment_answer.*
import android.widget.RadioButton
import iit.uvip.twoafctemporalquestapp.getTimeDifference
import iit.uvip.twoafctemporalquestapp.showToast
import java.util.*




class AnswerDialogFragment: DialogFragment()
{
    val LOG_TAG = AnswerDialogFragment::class.java.simpleName

    lateinit var onsetDate:Date

    companion object {
        fun newInstance(title: String): AnswerDialogFragment {
            val frag = AnswerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch arguments from bundle and set title
        val title       = arguments!!.getString("title", "Enter Name")
        val str_trial   = "trial " +  (arguments!!.getInt("trial_id", 0) + 1).toString() + " di " + arguments!!.getInt("tot_trials", 0)
        val question    = arguments!!.getString("question", "Enter Name")
        val answ1    = arguments!!.getString("answer1", "")
//        val answ2    = arguments!!.getString("answer2", "")
        val answ3    = arguments!!.getString("answer3", "")

        dialog?.setTitle(title)

        txt_trials.text     = str_trial
        txt_question.text   = question
        rb1.text            = answ1
        rb3.text            = answ3

        onsetDate           = Date()
    }

    override fun onResume() {
        // Get existing layout params for the window
        val params = dialog?.window!!.attributes
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window!!.attributes = params as android.view.WindowManager.LayoutParams

        super.onResume()

        bt_confirm.setOnClickListener{

            val elapsedms = getTimeDifference(onsetDate)
            when(radioGroup.checkedRadioButtonId != -1) {
                true -> {
                    val id                      = radioGroup.checkedRadioButtonId
                    val radioButton:RadioButton = radioGroup.findViewById(id)
                    val radioId                 = radioGroup.indexOfChild(radioButton)      // val btn = radioGroup.getChildAt(radioId) as RadioButton

                    sendResult(Test.EVENT_ANSWER_GIVEN, elapsedms, radioId)
                }
                false -> showToast("Seleziona un'opzione", context!!)
            }
        }

        bt_repeat.setOnClickListener{
            sendResult(Test.EVENT_TRIAL_REPEAT, 0, null)
        }

        bt_abort_test.setOnClickListener{
            sendResult(Test.EVENT_TRIAL_ABORT, 0, null)
            dismiss()
        }
    }



    private fun sendResult(response_code:Int, elapsedTime:Int, response_id:Int?) {
        if (targetFragment == null) {
            return
        }
        val intent = TestFragment.newIntent(response_code, elapsedTime, response_id)
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        dismiss()
    }
}