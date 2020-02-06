This App perform two classes of test:
- Bisection
- musical meters discrimination

1) Bisection

Three stimuli are delivered. first and third are separated by 1000ms. the second latency varies.
user must recognize if the second stimulus is closer (in time) to either the first or the third stimulus.

it has four modalities

- unimodal audio
- unimodal tactile (smartphone vibration)
- bimodal audio/tactile (with no conflict)
- bimodal audio/video with the stimuli delayed by 200ms in the second set of stimuli

Exported data: 
trial_id  stim_label(a/at/t/av) latency(#ms) conflict(none/av/va) result(true/false)  corr_answer(1/0)   user_answer(1/0)  elapsed(#ms)  repetitions


2) musical meters 

Two types of audios are playbacked. in the first class the two hemi tracks are equal, in the second class they are different.
user must recognize to which class the listened audio belongs to.

Exported Data:
trial_id  stim_label(same/diff)  result(true/false)  corr_answer(1/0)   user_answer(1/0)  elapsed(#ms)  repetitions audio_id(1->18)