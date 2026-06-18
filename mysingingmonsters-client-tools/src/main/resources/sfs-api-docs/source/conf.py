# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'Unofficial My Singing Monsters API Documentation'
copyright = '2026, glistermelon'
author = 'glistermelon'
release = '0.1.0'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = [
    'myst_parser'
]

default_dark_mode = True

source_suffix = {
    '.rst': 'restructuredtext',
    '.md': 'markdown'
}

templates_path = ['_templates']
exclude_patterns = []

myst_enable_extensions = [
    'amsmath',
    'dollarmath'
]


# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']
html_js_files = ['github.js']

def setup(app):
    app.add_css_file('css.css')