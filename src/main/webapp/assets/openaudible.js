// OpenAudible.js web page to display audio book library.

// filter out (hide) books not in filter text. If no filter text, all books are shown.
function filter() {
	const text = $("#filter").val();
	const rex = new RegExp(text, 'i');
	$('.searchable tr').hide();
	$('.searchable tr').filter(function () {
		return rex.test($(this).text());
	}).show();
}

function asString(str, len) {
	if (!str) return "";
	if (len) 
		str = str.substring(0, len);
	return str.replace(/(?:\r\n|\r|\n)/g, ' ').replace('  ', ' ');
}

function mp3Link(book, content) {
    if (!book || !book.mp3 || !content)
        return "";
    return "<a href='mp3/" + encodeURIComponent(book.mp3) + "'>" + content + "</a> ";
}
function authorLink(book) {
    if (!book || !book.author)
        return "";
    if (book.author_url && book.author_url.substring(0, 4) == 'http')
    {
        return "<a href='"+book.author_url + "'>" + asString(book.author) + "</a> ";
    }
    return asString(book.author);
}
function publisherLink(book) {
    if (!book || !book.publisher)
        return "";
    return "<a href='https://www.audible.com/search?searchProvider="+encodeURIComponent(book.publisher) + "'>" + asString(book.publisher) + "</a> ";
}
function narratorLink(book) {
    if (!book || !book.narrated_by)
        return "";
    return "<a href='https://www.audible.com/search?searchNarrator="+encodeURIComponent(book.narrated_by) + "'>" + asString(book.narrated_by) + "</a> ";
}

function bookImage(book, addLink) {
	var image = (book && book.image !== undefined) ? ("<img src='thumb/" + encodeURIComponent(book.image) + "' width='200' height='200'>")
		: "<img src='assets/no_cover.png' width='200' height='200'>";
	if (addLink) return mp3Link(book, image);
	return image;
}
// convert the json book data to a format for the book.
function populateBooks(arr, table) {

	let i;
	const data = [];

	for (i = 0; i < arr.length; i++) {
		const book = arr[i];

		// Thumbnail size 200x200
		const row = {};
		const narrated_by = asString(book.narrated_by);
		const author = asString(book.author);
		const title = asString(book.title);
		const duration = asString(book.duration);
		
		row['book'] = book;
		row['title'] = title;
		row['narrated_by'] = narratorLink(book);
		row['author'] = authorLink(book);	// author;
		row['duration'] = duration;
        row['publisher'] = publisherLink(book);	// publisher link

        row['purchase_date'] = asString(book.purchase_date);
        row['release_date'] = asString(book.release_date);
		row['rating'] = asString(book.rating_average);
		row['summary'] = asString(book.summary, 500);
		row['description'] = asString(book.description, 800);
		row['mp3'] = mp3Link(book, book.mp3);
		row['image'] = bookImage(book, true);
		
		let info = "<strong>" + title + "</strong><br>";
		if (author.length > 0)
			info += "by <i>" + author + "</i><br>";
		if (narrated_by.length > 0)
			info += "Narrated by " + narrated_by + "<br>";
		info += duration;
		info += " ";
		info += asString(book.rating_average, 99);
		row['info'] = info;

		data.push(row);
	}

	// create bootstrapTable and populate table with data.
	if (table && arr.length)
	{
		$(table).bootstrapTable( {data: data})
			.on('click-row.bs.table', function (e, row, elem) {
		showBook(row.book);     // row click handler.
		});
	}
	
	return data;
}

// display a single book in a modal dialog.
function showBook(book) {
    $("#detail_title").text(asString(book.title));

    const image = bookImage(book, true);
    $("#detail_image").html(image);

    $('#title').text(asString(book.title));
    $('#narrated_by').text(asString(book.narrated_by));
    $('#author').text(asString(book.author));
    $('#purchased').text(asString(book.purchased));
    let rating = asString(book.rating_average);

    if (rating) {
        if (book.rating_count)
            rating += " (" + book.rating_count + ")";
    }

    $('#rating').text(rating);
    $('#duration').text(asString(book.duration));
    const summary = asString(book.summary, 9999).replace(/(?:\r\n|\r|\n)/g, '<p />');
    $('#summary').text(summary);

    let audible = "";
    if (book.link_url) {
        audible = "<a href='"+ book.link_url + "'>" + book.audible + "</a>";
    }
    $('#audible').html(audible);
    var mp3 = mp3Link(book, book.mp3);
    if (mp3) {
    	$("#mp3").html(mp3Link(book, book.mp3));
        $("mp3_details").show();
	} else
	{
        $("mp3_details").hide();
	}
	$("#detail_modal").modal('show');
}

