#!/usr/bin/env python

import sys
import os.path
import gtk
import gtk.gdk
import xdot

__author__ = "Julien TOUS"

__version__ = "0.1"


class MyDotWindow(xdot.DotWindow):

    baseDir=""
    currentComp=""

    def __init__(self):
        xdot.DotWindow.__init__(self)
        self.widget.connect('clicked', self.on_url_clicked)
        self.widget.connect('right-clicked', self.on_url_right_clicked)
        self.widget.connect('escaped', self.on_escape_pressed)

    def on_url_clicked(self, widget, url, event):
        fileName, fileExtension = os.path.splitext(url)
        if fileExtension == ".dot":
            print(os.path.join(self.baseDir, url))
            self.currentComp=url
            self.open_file(os.path.join(self.baseDir, url))
        else:
            if os.path.isfile(url):
                os.system("eclipse " + url)
		print("eclipse " +url)
        return True

    def on_escape_pressed(self,toto):
	if (self.currentComp[0:6]=="Simple"):
            if not os.path.isfile(os.path.join(self.baseDir, self.currentComp[6:])):
	        dialog = gtk.MessageDialog(
                parent = self,
                buttons = gtk.BUTTONS_OK,
                message_format="Full view does not exist for this component")
                dialog.connect('response', lambda dialog, response: dialog.destroy())
                dialog.run()
                return True
            self.currentComp=self.currentComp[6:]
        else:
            print("".join(("Simple",self.currentComp)))
            if not os.path.isfile(os.path.join(self.baseDir, "".join(("Simple",self.currentComp)))):
                dialog = gtk.MessageDialog(
                parent = self,
                buttons = gtk.BUTTONS_OK,
                message_format="Simple view does not exist for this component")
                dialog.connect('response', lambda dialog, response: dialog.destroy())
                dialog.run()
                return True
            self.currentComp="".join(("Simple",self.currentComp))
        self.open_file(os.path.join(self.baseDir, self.currentComp))
        return True
    
    def on_url_right_clicked(self,toto):
        compPath=self.currentComp.split('.')
        if (2 >= len(compPath)):
            dialog = gtk.MessageDialog(
            parent = self,
            buttons = gtk.BUTTONS_OK,
            message_format="Allready at the top level component")
            dialog.connect('response', lambda dialog, response: dialog.destroy())
            dialog.run()
            return True
        self.currentComp=".".join(compPath[0:-2])+".dot"
        self.open_file(os.path.join(self.baseDir, self.currentComp))
        return True



def main():
    import optparse

    parser = optparse.OptionParser(
        usage='\n\t%prog [file]',
        version='%%prog %s' % __version__)

    win = MyDotWindow()
    win.connect('destroy', gtk.main_quit)

    (options, args) = parser.parse_args(sys.argv[1:])
    if len(args) < 1:
        dotfile = win.on_open(gtk.STOCK_OPEN);
	if dotfile == 0:
		return -1
    else:
        dotfile = args[0]
        win.open_file(dotfile)

    win.baseDir=os.path.abspath(os.path.dirname(dotfile))
    win.currentComp=os.path.basename(dotfile)

    gtk.main()

if __name__ == '__main__':
    main()
