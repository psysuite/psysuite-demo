package iit.uvip.audiotactilebindingapp.tests.atb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_atb.*
import kotlinx.android.synthetic.main.fragment_subject_info_basic.spCondition

// add whitenoise check button
class SubjectATBDialogFragment : SubjectBasicDialogFragment() {
    override val LOG_TAG: String = SubjectATBDialogFragment::class.java.simpleName


    companion object {
        fun newInstance(title: String): SubjectATBDialogFragment {
            val frag = SubjectATBDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_info_atb, container)
    }

    override fun updateGUI(subj: SubjectBasicParcel) {
        super.updateGUI(subj)
        swWhiteNoise.isChecked = (subj as SubjectATBParcel).whitenoise
    }

    override fun clear() {
        super.clear()
        swWhiteNoise.isChecked = true
    }

    override fun updateSubject(): SubjectATBParcel? {
        subject = super.updateSubject() as SubjectATBParcel
        (subject as SubjectATBParcel).whitenoise = swWhiteNoise.isChecked
        subject.type =
            TestATB.getConditionsInfo(requireContext())[spCondition.selectedItemPosition].id
        return subject as SubjectATBParcel
    }
}