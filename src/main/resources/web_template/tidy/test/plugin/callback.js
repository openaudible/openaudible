test('Global Checkbox', function() {
	var checkbox = $(table).find('thead th :checkbox');

	ok(checkbox.prop('checked', true), 'Select all rows checkbox checked');

	ok(checkbox.trigger('click'), 'Trigger checkbox event');

	$(table).find('tbody tr').each(function(index) {
		var row = $(this);

		ok(row.find(':checkbox').is(':checked'), 'Row ' + index + ' checkbox is checked');

		ok(row.hasClass('check_on'), "<tr> contains required class 'check_on'");
	});

	ok(checkbox.prop('checked', false), 'Deselect all rows checkbox checked');

	ok(checkbox.trigger('click'), 'Trigger checkbox event');

	$(table).find('tbody tr').each(function(index) {
		var row = $(this);

		ok(row.find(':checkbox').not(':checked'), 'Row ' + index + ' checkbox is unchecked');

		ok(row.hasClass('check_off'), "<tr> contains required class 'check_off'");
	});
});

test('Single Checkbox', function() {
	$(table).find('tbody tr').each(function(index) {
		var row = $(this);

		var checkbox = row.find(':checkbox');

		ok(checkbox.prop('checked', true), 'Row ' + index + ' checkbox checked');

		ok(checkbox.trigger('click'), 'Trigger checkbox event');

		ok(row.hasClass('check_on'), "<tr> contains required class 'check_on'");
	});

	$(table).find('tbody tr').each(function(index) {
		var row = $(this);

		var checkbox = row.find(':checkbox');

		ok(checkbox.prop('checked', false), 'Row ' + index + ' checkbox unchecked');

		ok(checkbox.trigger('click'), 'Trigger checkbox event');

		ok(row.hasClass('check_off'), "<tr> contains required class 'check_off'");
	});
});

test('Select Menu', function() {
	var menu = $('select.options');

	next['event1'] = true;

	ok(menu.val('1').change(), "Change menu 'Callback 1' option with no rows selected");

	equal(window.alert.message, 'callback1(rows=0)', "Window alert message expected is 'callback1(rows=0)'");

	var count1 = 0;

	$(table).find('tbody tr').each(function(index) {
		var row = $(this),
			sel = Math.random() >= 0.5;

		if (sel) {
			var checkbox = row.find(':checkbox');

			ok(checkbox.prop('checked', true), 'Row ' + index + ' checkbox checked');

			ok(checkbox.trigger('click'), 'Trigger checkbox event');

			checkbox.prop('checked', true);

			ok(row.hasClass('check_on'), "<tr> contains required class 'check_on'");

			count1++;
		}
	});

	ok(menu.val('1').change(), "Change menu 'Callback 1' option with " + count1 + " rows selected");

	var result1 = 'callback1(rows=' + count1 + ')';

	equal(window.alert.message, result1, "Window alert message expected is '" + result1 + "'");

	$(table).find('tbody tr').each(function(index) {
		$(this).find(':checkbox').prop('checked', false);
	});

	next['event1'] = false;

	next['event2'] = true;

	ok(menu.val('2').change(), "Change menu 'Callback 2' option with no rows selected");

	equal(window.alert.message, 'callback2(rows=0)', "Window alert message expected is 'callback2(rows=0)'");

	var count2 = 0;

	$(table).find('tbody tr').each(function(index) {
		var row = $(this),
			sel = Math.random() >= 0.5;

		if (sel) {
			var checkbox = row.find(':checkbox');

			ok(checkbox.prop('checked', true), 'Row ' + index + ' checkbox checked');

			ok(checkbox.trigger('click'), 'Trigger checkbox event');

			checkbox.prop('checked', true);

			ok(row.hasClass('check_on'), "<tr> contains required class 'check_on'");

			count2++;
		}
	});

	ok(menu.val('2').change(), "Change menu 'Callback 2' option with " + count2 + " rows selected");

	var result2 = 'callback2(rows=' + count2 + ')';

	equal(window.alert.message, result2, "Window alert message expected is '" + result2 + "'");

	$(table).find('tbody tr').each(function(index) {
		$(this).find(':checkbox').prop('checked', false);
	});

	next['event2'] = false;
});

test('Post-processing', function() {
	next['event3'] = true;

	ok($(table).trigger('mouseenter'), 'Trigger table hover event');

	var result3 = 'post-process(table)';

	equal(window.alert.message, result3, "Window alert message expected is '" + result3 + "'");

	next['event3'] = false;

	next['event4'] = true;

	$(table).find('tbody td[title]').each(function(index) {
		var col = $(this);

		ok(col.trigger('click'), "Trigger column '" + col.text() + "' click event");

		var result4 = 'post-process(value=' + col.text() + ')';

		equal(window.alert.message, result4, "Window alert message expected is '" + result4 + "'");
	});

	next['event4'] = false;

	next['event5'] = true;

	ok($('select.options').val('1').change(), 'Trigger menu change event');

	var result5 = 'post-process(menu)';

	equal(window.alert.message, result5, "Window alert message expected is '" + result5 + "'");

	next['event5'] = false;
});
