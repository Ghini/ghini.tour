ghini.tour
====================

Garden audio guide and geographic tour.

idea
--------------------

The ghini.tour app offers visitors something functionally similar to the
audio guides offered at the entrance of most modern musea.

The guide offers a detailed map of the garden on which a visitor can see
their precise position, and spoken text associated to spots in the garden,
and that's about it.

There is only one ghini.tour; each guide is just a ghini.tour configuration:
visitors download a configuration from the ghini.tour configuration menu,
use it during their visit, and may choose to remove it after they leave the
garden.

configuring and installing
-------------------------------

As a garden administrators willing to provide a ghini.tour of your garden,
you need to:

* Make sure that openstreetmap precisely and accurately describes your garden,
* Write an introduction text for your garden,
* Provide a set of points of interest (POIs) for your garden, each with its
  own text description, in as many languages as you think appropriate,
* Apart from the openstreetmap edits, send the data to me, so I can include
  it in our data repository.
* Some financial support would be welcome: hosting your data costs me money.
  
Garden visitors only need to:

* Install the free and ads-free ghini.tour from google play,
* Start ghini.tour,
* Select and download the garden among the available tours,

Although we talk about gardens and botany, the guides you can use with
ghini.tour are not at all limited to botanic gardens.

keeping track of tours
----------------------------

We haven't decided yet, but I wish to

* Let ghini.tour download POIs, texts and audio from ghini.me,

* Have tour texts on weblate, with one configuration marked as the "source"
  and then translations to whatever other languages we desire,
* The server at ghini.me would be producing the audio files automatically,
  (probably not: the quality of automatic TTS is maybe good enough for
  personal use, but not really for a tour.)

explaining tasks
-----------------------------------

Among the tasks of the garden administrator we mentioned some that are not
so obvious and for which an explanation is in order.  Please consider the
following options.

providing a map
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#. **OpenStreetMap.org**: edit http://osm.org until it accurately describes your
   garden.
#. **GeoTIFF**: georeference a topographic map of your garden: for those who
   can't physically visit your garden, this provides all necessary
   information in order to editing osm.org.
#. provide a **topographic map** of your garden, and ask us to georeference it
   (it should contain a geographic grid): this results in a rather precise
   GeoTIFF.
#. provide a **realistic map** of your garden, and ask us to warp it: this
   results in a possibly usable GeoTIFF and works best if you are able to
   collect and provide GPS traces.

I think that providing a topographic map of your garden is the option that
best balances difficulties and guarantees the best result.
  
adding POI in the garden
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Let's first of all assume you already know what you want to say, and where.
That is, you know that in a specific spot of your garden you want to have a
specific panel, or maybe you even already have it physically, and you want
to send me its digital version.

Give each panel a unique number, so we can identify it by this number.

The best thing you could do to reduce costs downstream, it would be to save
each panel in its own plain text file, calling it as the panel number, and
give it a ``txt`` extension.  Bitter experience has demonstrated that
Windows and Mac, but in particular Mac, do not make this task easy at all,
and if you do manage to save things as I'm suggesting, they will save files
without making clear which encoding you're using, causing even more trouble
downstream, so if you don't feel comfortable with ``vi`` nor ``emacs``,
please just write all your panels in one office document and leave to others
the task of splitting and saving them properly.

Translate each panel to as many languages as you need, and keep the
translations separated.  If you do use text files, just keep each version in
its own directory.  If you're putting all panels in one office document,
call the document as the language.

Now to the task of pin-pointing the spots in the garden.  We also have
several options, and they are all equally viable.

1. Do it on **paper**:
   * using a realistic map of your garden, put a dot for each panel, and
     clearly number it.
2. Use **openstreetmap** and produce a **spreadsheet**:
   * open http://osm.org in a desktop browser and navigate to your garden.
   * Zoom in as far as possible.
   * Now for each of your panels:
      1. right click on the spot where you want to place the virtual panel,
   and choose ``show address``.
      2. This activates a side pane on your screen, with the precise
   coordinates of your intended location.
      3. Select, Copy, then Paste in the spreadsheet.
   * The spreadsheet you produce should contain as many rows as there are
     panels, and at least the two columns: one with the panel identifier,
     one with both coordinates as copied from openstreetmap.
   * You can also separate latitude and longitude if you wish, it won't do
     harm I guess.
3. Use **QGIS** and produce a **spatialite** database (for us downstream it is just
   as practical as the spreadsheet):
   * open your garden project in QGIS,
   * add either OSM or your own GeoTIFF layer,
   * create a spatialite layer:
     * choose type Point,
     * add a text column for the panel title,
     * and one for the panel content.
   * Now please enter each panel as a feature:
     * click on the panel location, a dialog will show, requiring you to
       enter the point id, the panel title, the panel content.
     * Repeat for each panel.
   * The result of all this is the spatialite database to which you added
     the Point layer.
  
technical notes (mostly to myself)
--------------------------------------

connect to the phone `adb -d shell` or emulator `adb shell`

connect to the POI database::

  sqlite3 /data/data/me.ghini.tour/databases/poi.db

initial GPS position for screenshots::

  7.59237;-80.9624

