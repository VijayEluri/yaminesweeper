package minesweeper.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;

public class Minefield {
	private int cols = 8; // 8,16,31
	private int rows = 8; // 8,16,16
	private int minesNum = 10; // 10,40,99

	private Collection collection;
	private Grid grid;

	private MinesLeft minesLeft = new MinesLeft();
	private int closedFieldsLeft;
	private Level level = new Level(this);
	protected boolean is_active;

	public void setCols(int cols) {
		this.cols = cols;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setMinesNum(int minesNum) {
		this.minesNum = minesNum;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int getMinesNum() {
		return minesNum;
	}

	public MinesLeft getMinesLeft() {
		return minesLeft;
	}

	public Level getLevel() {
		return level;
	}

	private void decrementClosedFieldsLeft() {
		closedFieldsLeft--;

		if (closedFieldsLeft - minesNum == 0) {
			GameTimer.getInstance().stop();
			deactivate();
			showCongratsDialog();
		}
	}

	private void deactivate() {
		is_active = false;
	}

	public void init() {
		closedFieldsLeft = cols * rows;
		minesLeft.setNum(minesNum);
		GameTimer.getInstance().init();
		is_active = true;

		initCollection();
		initFields();
		initWidget();
	}

	private void initCollection() {
		collection = new Collection(cols, rows);
		populateMines();
	}

	private void populateMines() {
		for (int i = 0; i < minesNum; i++) {
			int col, row;
			do {
				col = (int) Math.round(Math.random() * (double) (cols - 1));
				row = (int) Math.round(Math.random() * (double) (rows - 1));
			} while (collection.get(col, row) != null);
			collection.set(new Field(this, col, row, Field.MINE));
		}
	}

	private void initFields() {
		for (CollectionIterator iterator = collection.iterator(); iterator
				.hasNext();) {
			Field field = iterator.next();
			if (field == null) {
				field = new Field(this, iterator.getCol(), iterator.getRow(), 0);
				collection.set(field);
			}
		}
	}

	private void initWidget() {
		grid = getWidget();
		grid.clear();
		grid.resize(rows, cols);

		for (CollectionIterator iterator = collection.iterator(); iterator
				.hasNext();) {
			Field field = iterator.next();
			grid.setWidget(iterator.getRow(), iterator.getCol(), field
					.getWidget());
		}
	}

	public Grid getWidget() {
		if (grid == null) {
			grid = new Grid() {
				@Override
				public void onBrowserEvent(Event event) {
					if (is_active) {
						switch (event.getTypeInt()) {
						case Event.ONMOUSEUP:
						case Event.ONMOUSEDOWN:
							if (DOM.eventGetCurrentTarget(event) == getElement()) {
								elementClicked(event);
							}
							break;
						}
					}
					event.stopPropagation();
					event.preventDefault();
				}
			};
			grid.sinkEvents(Event.ONCONTEXTMENU | Event.ONMOUSEDOWN
					| Event.ONMOUSEUP | Event.ONDBLCLICK);
			grid.addStyleName("grid");
		}
		return grid;
	}

	private void elementClicked(Event event) {
		Element element = DOM.eventGetTarget(event);

		for (CollectionIterator iterator = collection.iterator(); iterator
				.hasNext();) {
			Field field = iterator.next();

			if (field.getCurrentWidget().getElement() == element) {
				field.clicked(event);
				break;
			}
		}
	}

	public void open(Field current_field) {
		decrementClosedFieldsLeft();

		if (current_field.isEmpty()) {
			for (AroundFieldIterator iterator = collection.aroundFieldIterator(
					current_field.getCol(), current_field.getRow()); iterator
					.hasNext();) {
				Field field = iterator.next();

				field.open();
			}
		}
		grid.setWidget(current_field.getRow(), current_field.getCol(),
				current_field.getWidget());
	}

	public Collection getCollection() {
		return collection;
	}

	public void boom() {
		GameTimer.getInstance().stop();
		deactivate();

		for (CollectionIterator iterator = collection.iterator(); iterator
				.hasNext();) {
			Field field = iterator.next();

			if (field.isMine() || field.isFlaged()) {
				field.setOpened(true);
				grid.setWidget(field.getRow(), field.getCol(), field
						.getWidget());
			}
		}
	}

	public void showOptionsDialog() {
		new OptionsDialog(this);
	}

	private void showCongratsDialog() {
		new CongratsDialog(this);
	}
}
