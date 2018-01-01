module('Tidy-Table', {
	setup: function() {
		$('#qunit-fixture')
			.TidyTable({
				enableCheckbox: true,
				enableMenu:     true,
				reverseSortDir: true
			},
			{
				columnTitles: ['Column A', 'Column B', 'Column C'],
				columnValues: [
					['Row 1A', 'Row 1B', 'Row 1C'],
					['Row 2A', 'Row 2B', 'Row 2C'],
					['Row 3A', 'Row 3B', 'Row 3C'],
					['Row 4A', 'Row 4B', 'Row 4C']
				],
				menuOptions: [
					['- Action -', null],
					['Callback 1', { callback: doSomething1 }],
					['Callback 2', { callback: doSomething2 }]
				],
				postProcess: {
					table:  doSomething3,
					column: doSomething4,
					menu:   doSomething5
				}
			});

		function doSomething1(rows) {
			if (next['event1']) {
				alert('callback1(rows=' + rows.length + ')');
			}
		}

		function doSomething2(rows) {
			if (next['event2']) {
				alert('callback2(rows=' + rows.length + ')');
			}
		}

		function doSomething3(table) {
			table.on('hover', function() {
				if (next['event3']) {
					alert('post-process(table)');
				}
			});
		}

		function doSomething4(col) {
			col.on('click', function() {
				if (next['event4']) {
					alert('post-process(value=' + $(this).text() + ')');
				}
			});
		}

		function doSomething5(menu) {
			menu.on('change', function() {
				if (next['event5']) {
					alert('post-process(menu)');
				}
			});
		}
	},
	teardown: function() {
		// do nothing - preserve element structure
	}
});

test('Generate HTML', function() {
	ok($('#qunit-fixture').find(table), 'Table elements created');
});
