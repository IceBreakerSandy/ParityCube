package demo.paritycube.com.deals.core.navigation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;

import java.util.Arrays;
import java.util.Locale;
import java.util.Stack;

import demo.paritycube.com.deals.R;
import demo.paritycube.com.deals.core.BaseFragment;


public class NavigationFragment extends BaseFragment
                             implements NavigationHandler,
                                        NavigationBarHandler
{
  /* Properties */

  private NavigationBarHandler m_navBarHandler;

  private Stack<String> m_fragmentTagStack = new Stack<>();

  /* Fragment life-cycle methods */

  @Override
  public void onAttach (Context context)
  {
    super.onAttach(context);

    Fragment parentFragment;
    Activity activity = getActivity();
    if (activity instanceof  NavigationBarHandler)
    {
      m_navBarHandler = (NavigationBarHandler) activity;
    }
    else if (  (parentFragment = getParentFragment()) != null
             && parentFragment instanceof NavigationBarHandler)
    {
      m_navBarHandler = (NavigationBarHandler) parentFragment;
    }
  }

  @Override
  public void onCreate (Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    /* Restore fragment tag stack if able. */
    if (savedInstanceState != null)
    {
      String stack = savedInstanceState.getString("stack");
      if (stack != null)
      {
        String[] stacks = stack.split(";");
        m_fragmentTagStack.addAll(Arrays.asList(stacks));
      }
    }
  }

  @Override
  public void onSaveInstanceState (Bundle outState)
  {
    super.onSaveInstanceState(outState);

    int size = m_fragmentTagStack.size();
    if (size == 0)
    {
      return;
    }

    /* Save fragment tag stack. */
    StringBuilder builder = new StringBuilder();
    for (String tag : m_fragmentTagStack)
    {
      builder.append(tag);
      builder.append(";");
    }
    if (builder.length() > 0)
    {
      builder.deleteCharAt(builder.length() - 1);
    }
    outState.putString("stack", builder.toString());
  }

  /* Custom callback methods */

  @Override
  public boolean onKeyDown (int keyCode, KeyEvent event)
  {
    boolean handled = false;
    Fragment fragment = getCurrentFragment();
    if (   fragment != null
        && fragment instanceof  BaseFragment)
    {
      handled = ((BaseFragment) fragment).onKeyDown(keyCode, event);
    }

    if (   !handled
        && keyCode == KeyEvent.KEYCODE_BACK)
    {
      if (getBackStackSize() > 0)
      {
        handled = true;
        popFragmentFromNavigation(true);
      }
      else
      {
        handled = super.onKeyDown(keyCode, event);
      }
    }
    return handled;
  }

  /* Property methods */

  /**
   * Returns the number of entries currently in the back stack.
   */
  public int getBackStackSize ()
  {
    return Math.max(0, getStack().size() - 1);
  }

  @Override
  public Toolbar getToolbar ()
  {
    return m_navBarHandler != null ? m_navBarHandler.getToolbar() : null;
  }

  /* Navigation methods */

  /**
   * See {@link #setRootFragment(Fragment, String)}.
   */
  public void setRootFragment (Fragment fragment)
  {
    setRootFragment(fragment, null);
  }

  /* Navigation handler methods */

  /**
   * Called navigation has changed. This give chance to listen to navigation change.
   */
  @CallSuper
  protected void onNavigationChanged (Fragment oldFragment, Fragment newFragment)
  {
    /* Clean toolbar old state. */
    Toolbar toolbar = getToolbar();

    Menu menu = toolbar.getMenu();
    if (menu.size() > 0)
    {
      menu.clear();
    }
  }

  @Override
  public void setRootFragment (Fragment fragment, String tag)
  {
    Stack<String> stack = getStack();
    FragmentManager manager = getChildFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();

    /* Clear all fragment from stack. */
    for (String stackTag : stack)
    {
      Fragment stackFragment = retrieveFragmentByTag(stackTag);
      transaction.remove(stackFragment);
    }
    stack.clear();

    /* Set root boolean flag if able. */
    if (fragment instanceof NavigationItemFragment)
    {
      ((NavigationItemFragment) fragment).setIsRoot(true);
    }

    /* If no string tag is specified. Generate default fragment tag. Later to be
     * used to retrieve the fragment in the stack.
     */
    if (tag == null || tag.isEmpty())
    {
      tag = generateDefaultTagForIndex(0);
    }

    transaction.setCustomAnimations(
        R.anim.fragment_show_enter,
        R.anim.fragment_show_exit);

    transaction.replace(R.id.content, fragment, tag);
    transaction.commitNow();

    stack.push(tag);

    /* Inform navigation changed. */
    onNavigationChanged(null, fragment);
  }

  @Override
  public void pushFragmentToNavigation (Fragment fragment, String tag,
                                        boolean animated)
  {
    Stack<String> stack = getStack();
    FragmentManager manager = getChildFragmentManager();
    FragmentTransaction transaction = manager.beginTransaction();

    /* If no string tag is specified. Generate default fragment tag. Later to be
     * used to retrieve the fragment in the stack.
     */
    int stackSize = stack.size();
    if (tag == null || tag.isEmpty())
    {
      tag = generateDefaultTagForIndex(stackSize);
    }
    stack.push(tag);

    if (animated)
    {
      transaction.setCustomAnimations(
          R.anim.fragment_push_enter,
          R.anim.fragment_push_exit);
    }

    /* Retrieve current fragment instance and hide if able. */
    Fragment curFragment = null;
    if (stackSize > 0)
    {
      String curFragmentTag = stack.get(stackSize - 1);
      curFragment = retrieveFragmentByTag(curFragmentTag);
      transaction.detach(curFragment);
    }

    transaction.add(R.id.content, fragment, tag);
    transaction.commitNow();

    /* Inform navigation changed. */
    onNavigationChanged(curFragment, fragment);
  }

  @Override
  public void popFragmentFromNavigation (boolean animated)
  {
    Stack<String> stack = getStack();
    int stackSize = stack.size();
    if(stackSize > 1)
    {
      FragmentManager manager = getChildFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();

      String curFragmentTag = stack.pop();
      String prevFragmentTag = stack.get(stackSize - 2);

      /* Retrieve current and previous fragment instances. */
      Fragment curFragment = retrieveFragmentByTag(curFragmentTag);
      Fragment prevFragment = retrieveFragmentByTag(prevFragmentTag);

      if (animated)
      {
        transaction.setCustomAnimations(
            R.anim.fragment_pop_enter,
            R.anim.fragment_pop_exit);
      }

      transaction.remove(curFragment);

      /* Checks if the previous fragment is detach from UI. See
       * onSaveInstanceState(Bundle).
       */
      if (prevFragment.isDetached())
      {
        transaction.attach(prevFragment);
      }

      if (prevFragment.isHidden())
      {
        transaction.show(prevFragment);
      }
      transaction.commitNow();

      /* Inform navigation changed. */
      onNavigationChanged(curFragment, prevFragment);
    }
  }

  /* Internal methods */

  /**
   * Retrieve fragment stack.
   */
  protected Stack<String> getStack ()
  {
    return m_fragmentTagStack;
  }

  /**
   * Returns the current fragment.
   */
  protected Fragment getCurrentFragment ()
  {
    Fragment fragment = null;
    Stack<String> tagBackStack = getStack();
    if (!tagBackStack.isEmpty())
    {
      String topTag = tagBackStack.get(tagBackStack.size() - 1);
      fragment = retrieveFragmentByTag(topTag);
    }
    return fragment;
  }

  /**
   * Returns a default string fragment tag for the specified index from the
   * stack.
   */
  protected String generateDefaultTagForIndex (int stackIndex)
  {
    return String.format(Locale.US, "s%d", stackIndex);
  }
}
