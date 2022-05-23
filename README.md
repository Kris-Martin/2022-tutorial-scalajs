## A little Scala.js and Veautiful tutorial

This is a quick tutorial to demonstrate Scala.JS and Veautiful

As it's a Scala.js project, it will expect you to have [Node.js](https://nodejs.org/en/download/) installed as well as [SBT](https://www.scala-sbt.org/). 

If you prefer to work with Docker and have Docker Desktop installed, there is config for a devcontainer including node, etc, included in the repository. "Reopen in Container" from Visual Studio Code and it should set up a container for you.

Or you can run it from a server that already has Node and SBT, such as your university student servers.

The project contains some code for a command-line Wordle-like game. We'll play around with putting some
of it into a web page.

**Note**: Each of the steps involves updating the code and I often give you a new function to paste in. 
If you're feeling lazy or don't want to do any typing, the `working` branch's history contains each of 
these steps. Using your favourite git GUI, you can check out each of the steps in turn and try it without 
having to paste the code yourself.


### Test the JVM project works

First, load the project into sbt and run the tests. 

They're not very interesting tests, but they run.

The project contains some cut-down code of a Wordle game. Cut down because we've removed all the File IO
and StdIn.

Maybe `sbt run` too to see what it does - picks a random 5 letter word (from a very short list) and
tests it against another random word from the same list.

### Downloading the Scala.js SBT plugin

The first step has been done for you. Open `project/plugins.sbt` and you will see

```sbt
// Added Scala.js plugin
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.0")
```

This will download the Scala.js build plugin for SBT. It won't automatically add it to your project,
however.



### Enable the Scala.JS plugin

Open `build.sbt` and insert the line `.enablePlugins(ScalaJSPlugin)` like so:

```scala
lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Scala.js tutorial",
    version := "2022.1",
    scalaVersion := "3.1.2", 

    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test
  )
```

At the sbt prompt, reload the project (`reload`) and then re-run the tests (`test`)

You should notice SBT download the Scala.js version of munit, and it will finish with output something like this:

```
[info] Fetched artifacts of 
[info] compiling 2 Scala sources to /workspaces/2022-tutorial-scalajs/target/scala-3.1.2/classes ...
[info] compiling 1 Scala source to /workspaces/2022-tutorial-scalajs/target/scala-3.1.2/test-classes ...
[info] Fast optimizing /workspaces/2022-tutorial-scalajs/target/scala-3.1.2/scala-js-tutorial-test-fastopt
cosc250.wordle.WordleSuite:
  + This is only a placeholder test 0.00s
[info] Passed: Total 1, Failed 0, Errors 0, Passed 1
[success] Total time: 9 s, completed May 23, 2022, 10:50:09 AM
```

It just compiled and ran some Scala.JS!

If you try to `sbt run`, that should run it ok too. But we want to run it in a browser.

### Start a local webserver

You can skip this and open it from a file in your browser, but I think it's nicer to see things
running in a webserver since that's the way things get delivered on the web.

From a new shell, in the same directory (where `build.sbt` is):

```sh
npx http-server -p 50123 -c-1
```

(Or some other high-numbered port. If you're on a shared server, e.g. a university student server, you'll
need to each pick different port numbers.)

That should start a little webserver, serving up the current directory on port `50123` (or whichever port
you picked)

The `-c-1` at the end tells it disable caching. Otherwise when we hit reload in the browser, we might not
know if we're seeing our latest changes or it's accidentally cached something. 

Open it in a web browser. (Remembering that if you're working on a university server, you'll probably
need to open the browser on the same server.) The URL is

```
http://localhost:50123
```

At the moment, it will probably just list the contents of the directory. (If you haven't got http-server installed, it might ask to install that first too - let it.)

### Create `index.html`

Let's create an `index.html` file (in the directory where `build.sbt` is).

```html
<!DOCTYPE html>
<html>
  <title>Scala.JS test</title>

  <div id="render-here">
      Hello world
  </div>

</html>
```

Hit refresh and instead of a directory listing, we should see `Hello world`.

### Load our JavaScript

Let's load our generated JavaScript.

For now, we're going to make our page load our *fast optimised* JavaScript. 
(Normally, we'd change that later to a better optimised version, but let's just use this in the tutorial).

In sbt, 

```
clean
fastOptJS
```

You should see it recompile and then give you something like:

```
[success] Total time: 1 s, completed May 23, 2022, 11:21:53 AM
sbt:Scala.js tutorial> fastOptJS
[info] compiling 2 Scala sources to /workspaces/2022-tutorial-scalajs/target/scala-3.1.2/classes ...
[info] Fast optimizing /workspaces/2022-tutorial-scalajs/target/scala-3.1.2/scala-js-tutorial-fastopt
[success] Total time: 2 s, completed May 23, 2022, 11:21:56 AM
```

but the path will depend on your computer. (`/workspaces/` is because I ran this in a Docker container)
If you look in the `target/3.1.2` directory, you should see `scala-js-tutorial-fastopt.js`

(The `3.1.2` might have changed if we've updated the version of Scala but I haven't edited the instructions)

Let's load that into `index.html`:

```html
<!DOCTYPE html>
<html>
  <title>Scala.JS test</title>

  <div id="render-here">
      Hello world
  </div>

  <script src="target/scala-3.1.2/scala-js-tutorial-fastopt.js"></script>

</html>
```

(Update the `3.1.2` if your Scala version is different!)

Open the developer tools in your browser from the menus, open the network tab, and reload the page.

You should see that as well as requesting `index.html`, it also loads `scala-js-tutorial-fastopt.js`

But nothing's run yet, because we haven't told it to use a `main` method

### Tell Scala.js to use a main method

Open `build.sbt` and we're going to insert the config setting `scalaJSUseMainModuleInitializer := true`:

```scala
lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Scala.js tutorial",
    version := "2022.1",
    scalaVersion := "3.1.2", 

    // This project uses a main method
    scalaJSUseMainModuleInitializer := true,

    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test
  )

testFrameworks += new TestFramework("munit.Framework")
```

Reload the project in sbt (`reload`) and re-generate the JS (`fastOptJS`)

Now reload the page in the browser and look in the developer console. You should see the program ran there!

### Interacting with the DOM

We don't really want to write a program just to run it in the console. Let's do some code to alter the
page.

JavaScript is *untyped* but Scala is a *typed* language. Somehow, we need to tell Scala how to access
the DOM. Let's do that the untyped way first.

Open `Wordle.scala` and replace `main` with this, including the import etc above it:

```scala
import scala.scalajs._
import js.annotation._

@js.native
@JSGlobalScope
object Globals extends js.Object {
  val document: js.Dynamic = js.native
}

// As this is a Scala.js project, I've removed the IO class (because File IO and StdIn don't exist)
// We're not (for this tutorial) going to worry too much about a little mutability
@main def runWordle = {
  val target = chooseWord
  val guess = chooseWord

  Globals.document.querySelector("#render-here").innerHTML = s"My word is $target"
}
```

From `sbt`, `fastOptJS` and reload the page.

You should see something like this in the page:

> My word is RAISE

What we did with the `Globals` object was declare a *facade* (a kind of fake object). We called it
`Globals`, which is entirely for our use. Within it, we declared the value `document` to be a native
variable from the JavaScript context. In JavaScript, `document` is one of the variables available in the
global scope.

We declared it as a `js.Dynamic`, which essentially means an unknown JavaScript type, so don't typecheck the contents.

So, then we were able to call methods on `Globals.document` and it would call JavaScript on the DOM

### Importing some types

Let's get rid of our dynamic import and instead add a library that will give us a typed API to the DOM

In build.sbt, we're going to add `libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0"`

```scala
lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Scala.js tutorial",
    version := "2022.1",
    scalaVersion := "3.1.2", 

    scalaJSUseMainModuleInitializer := true,

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.2.0",

    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test
  )

testFrameworks += new TestFramework("munit.Framework")
```

Now in `Wordle.scala`, get rid of that stuff about `Globals` and change `runWordle` to:

```scala
// As this is a Scala.js project, I've removed the IO class (because File IO and StdIn don't exist)
// We're not (for this tutorial) going to worry too much about a little mutability
@main def runWordle = {
  val target = chooseWord
  val guess = chooseWord

  import org.scalajs.dom
  dom.document.querySelector("#render-here").innerHTML = s"My word is $target"
}
```

From sbt, `reload` and `fastOptJS`. You should see it download the extra library (which is just a library of type declarations - it's not really adding much code to your JS output). Reload the page in the browser
and it should still work.

### Wordle in raw DOM

Let's change `runWordle` to play our game (sort-of):

```scala
// As this is a Scala.js project, I've removed the IO class (because File IO and StdIn don't exist)
// We're not (for this tutorial) going to worry too much about a little mutability
@main def runWordle = {

  import org.scalajs.dom
  
  // Get a reference to our div
  val top = dom.document.querySelector("#render-here")
  
  // Clear its contents
  top.innerHTML = ""
  
  // Set up a basic UI
  val pastGuesses = dom.document.createElement("div")
  val guessArea = dom.document.createElement("div")
  val guessBox = dom.document.createElement("input").asInstanceOf[dom.HTMLInputElement]
  val button = dom.document.createElement("button").asInstanceOf[dom.HTMLButtonElement]
  button.innerText = "Guess"
  top.append(pastGuesses, guessArea)
  guessArea.append(guessBox, button)

  // Choose a word
  val target = chooseWord

  button.onclick = { evt => 
    val guess = guessBox.value.toUpperCase
    val p = dom.document.createElement("pre")
    p.innerText = inOrder(checkString(target, guess)).toString
    guessArea.append(p)
  }
}
```

From sbt, `fastOptJS`, and reload the page.

Now, if you type guesses in the box, it will print them on the page and you can sort-of play it.

But:

* We had to do a typecast on the Button and Input elements to let Scala know the `onclick` and `value`
  members were there

* We're not getting our colourised string, because HTML doesn't understand ANSI colourised strings

* It all looks like very manual DOM hacking.

Let's instead pull in a little library I wrote, which is my own (personal) front-end that I like to use:
Veautiful.

### Importing Veautiful as a dependency

Open `build.sbt` and add JitPack as a repository. Also replace the import of the `scalajs-dom` library
with Veautiful. (Don't worry, Veautiful will still pull in scala-js-dom transitively)

```scala
resolvers += "jitpack" at "https://jitpack.io"

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "Scala.js tutorial",
    version := "2022.1",
    scalaVersion := "3.1.2", 

    scalaJSUseMainModuleInitializer := true,

    libraryDependencies += "com.github.wbillingsley.veautiful" %%% "veautiful" % "v0.3-SNAPSHOT",

    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test
  )

testFrameworks += new TestFramework("munit.Framework")
```

From sbt, `reload` and `fastOptJS` to regenerate the JS. It should download the library, but so far
if you open the web page nothing will have changed.

### Writing a Veautiful version of a React example

If you've used *React* or another font-end framework, you might be familiar with the idea of a "virtual DOM". In React, you don't manipulate real HTML elements, you just create a set of objects describing your
page and React makes any updates it deems necessary to make the page match what you asked for. They call this the "reconciliation" process.

That lets you declare your UI in a declarative way using JSX

```jsx
const root = ReactDOM.createRoot(document.getElementById('root'));
  
function tick() {
  const element = (
    <div>
      <h1>Hello, world!</h1>
      <h2>It is {new Date().toLocaleTimeString()}.</h2>
    </div>
  );
  root.render(element);
}

setInterval(tick, 1000);
```

Veautiful is a *mixed paradigm* front-end. It's not just a virtual DOM, but I won't get into the details for now. In any case, in Veautiful we can do something similar.

Edit `Wordle.scala` and put this into `runWordle`:

```scala
@main def runWordle = {
  import org.scalajs.dom
  import scala.scalajs.js.Date
  import com.wbillingsley.veautiful.html._

  val root = Attacher.newRoot(dom.document.getElementById("render-here"))

  val tick = () => {
    val element = <.div(
      <.h1("Hello world"),
      <.h2(s"It is ${new Date().toLocaleTimeString}")
    )

    root.render(element)
  }

  dom.window.setInterval(tick, 1000)
}
```

From sbt, `fastOptJS` and reload the page. It'll take a second to appear, because the `setInterval` only
gets called after a second (that's how `setInterval` works), but we have a ticking clock very similar to
the React version.

You'll notice some `<` and `^` in my code. Those are *just objects*. The notation is similar to scalatags,
which is another HTML library. They're supposed to be reminiscent of HTML angle brackets. `<` has methods
for creating elements. Methods for attributes and event handlers start with `^`. And it looks kind of like
HTML in our code, but it's just Scala.

### Writing a componemt

Ok, let's do our wordle-like game.

We're going to write a component that can keep some local state. For Veautiful, that is a `VHTMLComponent`
and it just requires us to implement a `render` method for it.

The biggest quirk is we use a `case class`. In this tiny example, it wouldn't matter, but in bigger 
examples it's useful to define components as case classes. (I won't bore you with the details, but it
gives us a way of deciding whether a component should be replaced or updated.)

Update `runWordle` to this:

```scala
@main def runWordle = {
  import org.scalajs.dom
  import scala.scalajs.js.Date
  import com.wbillingsley.veautiful.html._

  val root = Attacher.newRoot(dom.document.getElementById("render-here"))

  case class WordleGame(target:String) extends VHtmlComponent {

    // Let's define some local state for our guessing.
    case class WGS(past:List[String] = Nil, current:String = "", guesses:Int = 6)
    var state = WGS()

    // Updates the component state and re-renders
    def setState(s:WGS):Unit = {
      state = s
      rerender()
    }

    // A state update function for when we're making a guess
    def guess() = setState(state.copy(past = state.current :: state.past, "", state.guesses - 1))

    // A state update function for when we're typing a word
    def updateCurrent(s:String) = setState(state.copy(current = s))

    // Render our current state
    def render = {
      <.div(
        {
          if state.guesses > 0 then 
            <.div(
              <.input(
                ^.on("input") ==> { e => for s <- e.inputValue do updateCurrent(s) }, 
                ^.prop("value") := state.current
              ),
              <.button(^.on("click") --> guess(), "Guess"),
              s" ${state.guesses} guesses remaining"
            )
          else <.p(s"The word was ${target}")
        },
        <.div(
          <.h2("Past guesses:"),
          for w <- state.past yield <.p(inOrder(checkString(target, w.toUpperCase)).toString)
        )
      )

    }
  }

  root.render(WordleGame(chooseWord))
}
```

From sbt, `fastOptJS` and reload.

You should now have a playable Wordle game - albeit it doesn't colourise the letters.

Trivial things to notice:

* Because Scala is expression-oriented, we could put an `if ... else` directly into the render function.
  It's nothing special, just Scala code.

* We were also able to put the `for ... yield` directly into the render function.

* I wrote the state as a little case class, `WGS`, but I didn't have to. It just kept things neat.

Ok, let's do the last part and colourise some letters.

### Colourising the letters

The update to colourise the letters isn't very big. We just need a function that'll take two strings,
run them through our code, and then produce appropriate "VNodes" (kind of like virtual nodes) for the HTML.

This should do:

```scala
  def colourisedHtml(pairs:Seq[(Color, Char)]) = <.p(^.cls := "wordled", 
    for (col, char) <- pairs yield <.span(^.cls := col.toString, char.toString)
  )
```

Where `^.cls := col.toString` sets the CSS class name of the `span` element to the name of the colour.

Now, we could jump into the HTML and set the CSS styles of these. But as I'm going to paste in the last version of the code anyway, let's also do something funky: let's make *our CSS code too*.

"CSS in JS" is a relatively recent web development. If styles are in *code* rather than in *CSS*, then 
they're easier to publish as a library.

We're going to programmatically install a StyleSuite into the page:

```scala
  given mySiteStyles:StyleSuite = StyleSuite()
  val wordleStyle = Styling("border-radius: 10px; background: aliceblue; padding: 40px;")
    .modifiedBy(
      " .Green" -> "color: #00aa00; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;",
      " .Gray" -> "color: #888888; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;",
      " .Orange" -> "color: #ffaa00; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;"
    )
    .register()
  mySiteStyles.install()
```

This automatically installs a CSS stylesheet, with the program randomly creating a unique name for our
top level `wordleStyle`. In the `modifiedBy` lines, we defined how descendent elements should be styled,
effectively creating selectors like

```css
.wordleStyle .Green {
    // etc
}
```

Except that to avoid collision, our style actually has a randomly generated uniqe class name. Then we're going to tell our game's render to us it by

```scala
    def render = {
      <.div(^.cls := wordleStyle.className, 
        // etc
```


That sounds tricky to describe how to make the changes bit by bit, so make `runWordle` look like this:

```scala
@main def runWordle = {
  import org.scalajs.dom
  import scala.scalajs.js.Date
  import com.wbillingsley.veautiful.html._

  val root = Attacher.newRoot(dom.document.getElementById("render-here"))

  def colourisedHtml(pairs:Seq[(Color, Char)]) = <.p(
    for (col, char) <- pairs yield <.span(^.cls := col.toString, char.toString)
  )

  given mySiteStyles:StyleSuite = StyleSuite()
  val wordleStyle = Styling("border-radius: 10px; background: aliceblue; padding: 40px;")
    .modifiedBy(
      " .Green" -> "color: #00aa00; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;",
      " .Gray" -> "color: #888888; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;",
      " .Orange" -> "color: #ffaa00; font-size: 24px; text-align: center; padding: 5px; border: 1px solid #aaa; border-radius: 5px; display: inline-block; width: 30px;"
    )
    .register()
  mySiteStyles.install()


  case class WordleGame(target:String) extends VHtmlComponent {

    // Let's define some local state for our guessing.
    case class WGS(past:List[String] = Nil, current:String = "", guesses:Int = 6)
    var state = WGS()

    // Updates the component state and re-renders
    def setState(s:WGS):Unit = {
      state = s
      rerender()
    }

    // A state update function for when we're making a guess
    def guess() = setState(state.copy(past = state.current :: state.past, "", state.guesses - 1))

    // A state update function for when we're typing a word
    def updateCurrent(s:String) = setState(state.copy(current = s))

    // Render our current state
    def render = {
      <.div(^.cls := wordleStyle.className, 
        {
          if state.guesses > 0 then 
            <.div(
              <.input(
                ^.on("input") ==> { e => for s <- e.inputValue do updateCurrent(s) }, 
                ^.prop("value") := state.current
              ),
              <.button(^.on("click") --> guess(), "Guess"),
              s" ${state.guesses} guesses remaining"
            )
          else <.p(s"The word was ${target}")
        },
        <.div(
          <.h2("Past guesses:"),
          for w <- state.past yield colourisedHtml(inOrder(checkString(target, w.toUpperCase)))
        )
      )

    }
  }

  root.render(WordleGame(chooseWord))
}
```

From sbt, `fastOptJS` and refresh the web page. Your wordle game should look a little bit better when
you make guesses.

Normally, we wouldn't do it all in one function, but it was easier to give you instructions just to keep replacing that function.

Normally, we also wouldn't just link fastOptJS directly into the page - a lot of my repositories integrate
WebPack and use npm libraries (often `marked` for markdown parsing) as well as Scala libraries.

But I think this'll do for a little tutorial playing around with my library.