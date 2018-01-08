package nova.typoapp.wordpuzzle;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WordPuzzleEndFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WordPuzzleEndFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordPuzzleEndFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public WordPuzzleEndFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordPuzzleEndFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordPuzzleEndFragment newInstance(String param1, String param2) {
        WordPuzzleEndFragment fragment = new WordPuzzleEndFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }





    @BindView(R.id.tvPuzzleRecord)
    TextView tvPuzzleRecord;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_word_puzzle_end, container, false);

        ButterKnife.bind(this, view);


        WordPuzzleActivity activity = (WordPuzzleActivity)getActivity();

        String puzzleRecord = activity.getPuzzleRecord();

        tvPuzzleRecord.setText(puzzleRecord);

        // Inflate the layout for this fragment
        return view;
    }

    @OnClick(R.id.btnReplay)
    void onClickStart(){

//        Toast.makeText(getContext(), "change!", Toast.LENGTH_SHORT).show();
        android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        WordPuzzlePlayFragment playFragment = new WordPuzzlePlayFragment();
        Bundle bundle = new Bundle();
        playFragment.setArguments(bundle);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.containerFragmentPuzzle, playFragment); // Activity 레이아웃의 View ID
        fragmentTransaction.commit();


        //타이머를 보이게 하고,
        TextView tvTimer = (TextView) getActivity().findViewById(R.id.tvPuzzleTime);
        tvTimer.setVisibility(View.VISIBLE);


        //퍼즐 액티비티를 호출하여, 타이머 스레드를 실행한다.
        WordPuzzleActivity activity = (WordPuzzleActivity)getActivity();

        activity.startTimer();



    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
