package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.olafneumann.trainer.data.QuestionItem;
import org.olafneumann.trainer.data.QuestionItemListener;

class TrainerWindow extends JFrame implements QuestionItemListener {
	private static final long serialVersionUID = 2174230821028451129L;

	private static TrainerWindow window = null;

	public static synchronized void showWindow(final JFrame owner, final QuestionItem item,
			final boolean showCaptions) {
		if (window == null && item != null) {
			SwingUtilities.invokeLater(() -> {
				window = new TrainerWindow(owner, item, showCaptions);
				window.setExtendedState(owner.getExtendedState());
				window.setVisible(true);
				if (window.getExtendedState() == ICONIFIED) {
					window.setExtendedState(owner.getExtendedState());
				}
				KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(window.keyEventDispatcher);
			});
		} else {
			SwingUtilities.invokeLater(() -> window.requestFocus());
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			SwingUtilities.invokeLater(() -> {
				if (window.item != null) {
					window.item.removeQuestionItemListener(window);
				}

				window.setVisible(false);
				window.dispose();
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.removeKeyEventDispatcher(window.keyEventDispatcher);
				window = null;
			});
		}
	}

	private final KeyEventDispatcher keyEventDispatcher = new KeyEventDispatcher() {
		private boolean isInThisWindow(final Object source) {
			if (source instanceof KeyEvent) {
				return isInThisWindow(((KeyEvent) source).getSource());
			}
			if (source instanceof Component) {
				final Component component = (Component) source;
				if (component instanceof Frame) {
					return component == TrainerWindow.this;
				}
				return isInThisWindow(component.getParent());
			}

			return false;
		}

		@Override
		public boolean dispatchKeyEvent(final KeyEvent event) {
			if (isInThisWindow(event) && event.getID() == KeyEvent.KEY_TYPED) {
				switch (event.getKeyChar()) {
				case 'n':
				case 'N':
				case '\n':
				case '\r':
				case '3':
					getNextData(false);
					// System.out.println("next-false");
					return true;
				case 'r':
				case 'R':
				case 'y':
				case 'Y':
				case 'j':
				case 'J':
				case '1':
					getNextData(true);
					// System.out.println("next-true");
					return true;
				case 'q':
				case 'Q':
				case '4':
					hideWindow();
					// System.out.println("ende im gel??nde");
					return true;
				case 'h':
				case 'H':
				case '2':
					setAnswerAreaVisibility(!areAnswersVisible());
					// System.out.println("help!");
					return true;
				default:
					return false;
				}
			}
			return false;
		}
	};

	private final TrainArea txtQuestion;
	private final TrainArea[] txtAnswers;
	private final JButton btnNext;
	private final JButton btnGewusst;
	private final JButton btnShowHide;
	private final JButton btnClose;
	private final JButton btnBack;
	private final JButton btnForward;

	private QuestionItem item = null;
	private final Stack<QuestionItem> historyBack = new Stack<>();
	private final Stack<QuestionItem> historyForward = new Stack<>();

	private TrainerWindow(final JFrame owner, final QuestionItem item, final boolean showCaptions) {
		this.item = item;
		setIconImages(Icons.LOGO.getImageList());
		setTitle(owner.getTitle() + " - " + Messages.getString("TrainerWindow.Title")); //$NON-NLS-1$ //$NON-NLS-2$
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(800, 600));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				hideWindow();
			}
		});

		btnBack = new JButton(Icons.BUTTON_BACK.getImageIcon());
		btnForward = new JButton(Icons.BUTTON_FORWARD.getImageIcon());
		btnNext = new JButton(Icons.NEXT.getImageIcon());
		btnGewusst = new JButton(Icons.CHECK.getImageIcon());
		btnShowHide = new JButton(Icons.SHOW_HIDE.getImageIcon());
		btnClose = new JButton(Icons.CLOSE.getImageIcon());
		txtQuestion = new TrainArea(showCaptions, false);
		txtAnswers = new TrainArea[item.getAnswers().length];
		for (int i = 0; i < txtAnswers.length; ++i) {
			txtAnswers[i] = new TrainArea(showCaptions, true);
		}
		btnNext.setToolTipText(Messages.getString("TrainerWindow.ShowNextVocab")); //$NON-NLS-1$
		btnGewusst.setToolTipText(Messages.getString("TrainerWindow.IKnowThatVocab")); //$NON-NLS-1$
		btnClose.setToolTipText(Messages.getString("TrainerWindow.CloseTrainer")); //$NON-NLS-1$
		btnShowHide.setToolTipText(Messages.getString("TrainerWindow.ShowHideExplanation")); //$NON-NLS-1$

		setLayout(new BorderLayout());
		final JPanel pnlButtons = new JPanel();
		final JPanel pnlButtonsTop = new JPanel();
		final JPanel pnlButtonsMain = new JPanel();
		pnlButtons.setLayout(new BorderLayout());
		pnlButtonsTop.setLayout(new GridLayout(1, 2));
		pnlButtonsMain.setLayout(new GridLayout(4, 1));
		pnlButtonsMain.setMinimumSize(new Dimension(400, 100));

		final JPanel pnlAnswers = new JPanel(new GridLayout(1, txtAnswers.length));
		for (final JComponent txtAnswer : txtAnswers) {
			pnlAnswers.add(txtAnswer);
		}

		add(txtQuestion, BorderLayout.NORTH);
		add(pnlAnswers, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.EAST);
		pnlButtonsTop.add(btnBack);
		pnlButtonsTop.add(btnForward);
		pnlButtonsMain.add(btnGewusst);
		pnlButtonsMain.add(btnShowHide);
		pnlButtonsMain.add(btnNext);
		pnlButtonsMain.add(btnClose);
		pnlButtons.add(pnlButtonsTop, BorderLayout.NORTH);
		pnlButtons.add(pnlButtonsMain, BorderLayout.CENTER);

		final HyperlinkListener hll = hle -> {
			if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
				final URL url = hle.getURL();
				try {
					LinkRunner.open(url.toURI());
				} catch (final URISyntaxException e) {
					e.printStackTrace();
				}
			}
		};

		txtQuestion.addHyperlinkListener(hll);
		for (final TrainArea txtAnswer : txtAnswers) {
			txtAnswer.addHyperlinkListener(hll);
		}

		setLocationRelativeTo(owner);

		btnClose.addActionListener(e -> hideWindow());
		btnShowHide.addActionListener(e -> setAnswerAreaVisibility(!areAnswersVisible()));
		btnGewusst.addActionListener(e -> getNextData(true));
		btnNext.addActionListener(e -> getNextData(false));
		btnBack.addActionListener(e -> doGoBack());
		btnForward.addActionListener(e -> doGoForward());
		questionItemChanged(item);
		doEnablingDisabling();
		pack();
	}

	private boolean areAnswersVisible() {
		for (final TrainArea txtAnswer : txtAnswers) {
			if (!txtAnswer.isTextVisible()) {
				return false;
			}
		}
		return true;
	}

	private void setAnswerAreaVisibility(final boolean visible) {
		for (final TrainArea txtAnswer : txtAnswers) {
			txtAnswer.setTextVisible(visible);
		}
	}

	private void getNextData(final boolean known) {
		addBackHistory(item, true);
		setQuestionItem(item.getNext(known));
	}

	private void setQuestionItem(final QuestionItem newItem) {
		if (item != null) {
			item.removeQuestionItemListener(this);
		}
		item = newItem;
		if (item != null) {
			item.addQuestionItemListener(this);
			questionItemChanged(item);
			setAnswerAreaVisibility(false);
			btnNext.requestFocusInWindow();
		} else {
			hideWindow();
		}
	}

	@Override
	public void questionItemChanged(final QuestionItem item) {
		txtQuestion.setContent(item.getQuestionInput(), item.getQuestion());
		setAnswerAreaVisibility(false);
		for (int i = 0; i < txtAnswers.length; ++i) {
			txtAnswers[i].setContent(item.getAnswerInputs()[i], item.getAnswers()[i]);
		}
		invalidate();
		repaint();
	}

	private void addBackHistory(final QuestionItem item, final boolean clearForward) {
		if (historyBack.isEmpty() || !historyBack.peek().equals(item)) {
			historyBack.push(item);
			if (clearForward) {
				historyForward.clear();
			}
		}
		doEnablingDisabling();
	}

	private void addForwardHistory(final QuestionItem item) {
		if (historyForward.isEmpty() || !historyForward.peek().equals(item)) {
			historyForward.push(item);
		}
		doEnablingDisabling();
	}

	private void moveHistoryItem(final boolean back) {
		if (back) {
			if (!historyBack.isEmpty()) {
				setQuestionItem(historyBack.pop());
				addForwardHistory(item);
			}
		} else if (!historyForward.isEmpty()) {
			setQuestionItem(historyForward.pop());
			addBackHistory(item, false);
		}
		doEnablingDisabling();
	}

	private void doGoBack() {
		moveHistoryItem(true);
	}

	private void doGoForward() {
		moveHistoryItem(false);
	}

	private void doEnablingDisabling() {
		btnBack.setEnabled(!historyBack.isEmpty());
		btnForward.setEnabled(!historyForward.isEmpty());
	}
}
