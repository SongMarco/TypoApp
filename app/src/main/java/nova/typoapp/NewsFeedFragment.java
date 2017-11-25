package nova.typoapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.dummy.NewsFeedContent;
import nova.typoapp.dummy.NewsFeedContent.FeedItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class NewsFeedFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFeedFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewsFeedFragment newInstance(int columnCount) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }


    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.layoutAdd)
    LinearLayout layoutAdd;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfeed_list, container, false);

        ButterKnife.bind(this, view);

        //DB에서 회원 정보를 확인하고 로그인하라. 단, 두번은 안하게!

        //call이 되었다면 해당 어싱크를 수행하지 않는다.
        if(! NewsFeedContent.called ){
            NewsFeedContent.taskCallFeeds taskCall = new NewsFeedContent.taskCallFeeds();
            taskCall.execute();

        }

        // Set the adapter
        if (recyclerView != null) {
            Context context = view.getContext();

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyNewsFeedRecyclerViewAdapter(NewsFeedContent.ITEMS, mListener));
            recyclerView.setNestedScrollingEnabled(false);
        }


        //fab를 누르면 글쓰기로 넘어간다.
//        MainActivity.fabAdd.setVisibility(View.VISIBLE);


        return view;
    }


    @OnClick(R.id.layoutAdd)
    void onAddClick(){
        Toast.makeText(getContext(), "글을씁시다", Toast.LENGTH_SHORT).show();


    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnListFragmentInteractionListener) {


            mListener = (OnListFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FeedItem item);
    }
}
