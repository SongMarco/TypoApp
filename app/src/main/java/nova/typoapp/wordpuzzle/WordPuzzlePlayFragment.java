package nova.typoapp.wordpuzzle;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;

import static nova.typoapp.wordpuzzle.WordPuzzleActivity.gotItemsCopy;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WordPuzzlePlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WordPuzzlePlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WordPuzzlePlayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public WordPuzzlePlayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WordPuzzlePlayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WordPuzzlePlayFragment newInstance(String param1, String param2) {
        WordPuzzlePlayFragment fragment = new WordPuzzlePlayFragment();
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



    @BindView(R.id.rvWordPuzzle)
    RecyclerView rvWordPuzzle;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       final View view = inflater.inflate(R.layout.fragment_word_puzzle, container, false);
        ButterKnife.bind(this,view);


        MyWordPuzzleAdapter puzzleAdapter =new MyWordPuzzleAdapter(gotItemsCopy);

//        for(int i = 0; i < gotItemsCopy.size(); i ++){
//
//            gotItemsCopy.get(i).getItemInfo();
//
//        }
        rvWordPuzzle.setLayoutManager(new GridLayoutManager(getActivity(),3));

        rvWordPuzzle.setAdapter(puzzleAdapter);

        if(gotItemsCopy.size() != 0){
            //퍼즐 액티비티를 호출하여, 타이머 스레드를 실행한다.
            WordPuzzleActivity activity = (WordPuzzleActivity)getActivity();

            activity.runTimer();

        }



        return view;
    }












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


    @Override
    public void onResume() {
        super.onResume();

//        ArrayList<WordItem> playItems = new ArrayList<>(gotItemsCopy);


    }


    @Override
    public void onPause() {
//        Toast.makeText(getContext(), "onPause", Toast.LENGTH_SHORT).show();
        super.onPause();
    }

    @Override
    public void onStop() {
//        Toast.makeText(getContext(), "onStop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(getContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();



    }


    @Override
    public void onDetach() {
        super.onDetach();
//        Toast.makeText(getContext(), "onDetach", Toast.LENGTH_SHORT).show();
        mListener = null;
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
