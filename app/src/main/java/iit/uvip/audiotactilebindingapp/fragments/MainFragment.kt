package iit.uvip.audiotactilebindingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.atb.SubjectATBDialogFragment
import iit.uvip.audiotactilebindingapp.tests.atb.SubjectATBParcel
import iit.uvip.audiotactilebindingapp.tests.atb.TestATB
import iit.uvip.audiotactilebindingapp.tests.bis.TestBIS
import iit.uvip.audiotactilebindingapp.tests.common.TestBasic
import iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.audiotactilebindingapp.tests.mmd.TestMMD
import iit.uvip.audiotactilebindingapp.tests.tid.SubjectTIDDialogFragment
import iit.uvip.audiotactilebindingapp.tests.tid.SubjectTIDParcel
import iit.uvip.audiotactilebindingapp.tests.tid.TestTID
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var subject: SubjectBasicParcel
    override val LOG_TAG:String = MainFragment::class.java.simpleName

    companion object {
        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 2
        @JvmStatic val TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE: Int    = 3
        @JvmStatic
        val TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE: Int = 4
        @JvmStatic val EVENT_SUBJECT:String                             = "subject"
    }

    override fun onResume(){
        super.onResume()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_start_tid_test.setOnClickListener{
            showTIDSubjectDialog()
        }

        bt_start_atb_test.setOnClickListener {
            showATBSubjectDialog()
        }

        bt_start_bisection.setOnClickListener {
            showBISSubjectDialog()
        }

        bt_start_musicalmeter.setOnClickListener {
            showMMDSubjectDialog()
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showATBSubjectDialog(){

        subject = SubjectATBParcel.loadSubject()
        subject.taskcodes = TestATB.getConditionsInfo(requireContext())
        subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_BUTTON   // can choose whether pausing each trial

        val bundle  = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectATBDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showTIDSubjectDialog(){

        subject = SubjectTIDParcel.loadSubject()
        subject.taskcodes = TestTID.getConditionsInfo(requireContext())
        subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_ANSWER
        (subject as SubjectTIDParcel).spinner_data_resource = R.array.tid_sessions_array
        (subject as SubjectTIDParcel).first_modality = 0

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectTIDDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showBISSubjectDialog(){

        subject = SubjectBasicParcel.loadSubject()
        subject.taskcodes = TestBIS.getConditionsInfo(requireContext())
        subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_ANSWER

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this , TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showMMDSubjectDialog() {

        subject = SubjectBasicParcel.loadSubject()
        subject.taskcodes =
            TestMMD.getConditionsInfo(requireContext()) // taskcodes contains one element. subject.type is set automatically
        subject.nextTrailModality = TestBasic.TEST_NEXTTRIAL_ANSWER

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment.newInstance("Some Title")
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }
    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        // Make sure fragment codes match up

        if (data?.getParcelableExtra(EVENT_SUBJECT) as SubjectBasicParcel? == null)
            return

        when(requestCode){
            TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) as SubjectATBParcel
                (subject as SubjectATBParcel).writeJson(requireContext())
            }

            TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) as SubjectTIDParcel
                (subject as SubjectTIDParcel).writeJson(requireContext())
            }

            TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) as SubjectBasicParcel
                subject.writeJson(requireContext())
            }

            TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(EVENT_SUBJECT) as SubjectBasicParcel
                subject.writeJson(requireContext())
            }
        }

        val bundle = Bundle()
        bundle.putParcelable("test", subject)
        Navigation.findNavController(bt_start_bisection).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }
}